/*
 * Copyright (c) 2011-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import boofcv.alg.distort.LensDistortionWideFOV
import boofcv.alg.distort.spherical.MultiCameraToEquirectangular
import boofcv.alg.distort.universal.LensDistortionUniversalOmni
import boofcv.alg.interpolate.InterpolationType
import boofcv.factory.distort.FactoryDistort
import boofcv.factory.interpolate.FactoryInterpolation
import boofcv.io.calibration.CalibrationIO
import boofcv.io.image.ConvertBufferedImage
import boofcv.io.image.UtilImageIO
import boofcv.struct.border.BorderType
import boofcv.struct.calib.CameraUniversalOmni
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageType
import boofcv.struct.image.Planar
import georegression.geometry.ConvertRotation3D_F32
import georegression.geometry.UtilVector3D_F64
import georegression.metric.UtilAngle
import georegression.misc.GrlConstants
import georegression.struct.EulerType
import georegression.struct.point.Point3D_F64
import georegression.struct.se.Se3_F32
import kotlinx.coroutines.runBlocking
import org.ejml.dense.row.CommonOps_FDRM

import java.awt.image.BufferedImage
import java.io.File
import java.util.ArrayList
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

/**
 * Demonstrates how to combine multiple images together into a single view.  A 360 camera was used to generate
 * the two input fisheye images.  Each camera has been calibrated independently and the extrinsics between the two
 * cameras is assume to be known.  Because of how the fisheye image is modeled a mask is required to label pixels
 * outside the FOV that should not be considered.
 *
 * @author Peter Abeles
 */
class PanoMultiThread {

    /**
     * Creates a mask telling the algorithm which pixels are valid and which are not.  The field-of-view (FOV) of the
     * camera is known so we will use that information to do a better job of filtering out invalid pixels than
     * it can do alone.
     */
    private fun createMask(model: CameraUniversalOmni,
                           distortion: LensDistortionWideFOV, fov: Double): GrayU8 {
        val mask = GrayU8(model.width, model.height)

        val p2s = distortion.undistortPtoS_F64()
        val ref = Point3D_F64(0.0, 0.0, 1.0)
        val X = Point3D_F64()

        p2s.compute(model.cx, model.cy, X)

        for (y in 0 until model.height) {
            for (x in 0 until model.width) {
                p2s.compute(x.toDouble(), y.toDouble(), X)

                if (java.lang.Double.isNaN(X.x) || java.lang.Double.isNaN(X.y) || java.lang.Double.isNaN(X.z)) {
                    continue
                }

                val angle = UtilVector3D_F64.acute(ref, X)
                if (java.lang.Double.isNaN(angle)) {
                    continue
                }
                if (angle <= fov / 2.0)
                    mask.unsafe_set(x, y, 1)
            }
        }
        return mask
    }

    fun panoWritter(file1: String, file2: String, fisheyePath: String): BufferedImage {
        // Load fisheye RGB image

        val buffered0 = CompletableFuture.supplyAsync { return@supplyAsync UtilImageIO.loadImage(fisheyePath, file2) }
        val buffered1 = CompletableFuture.supplyAsync { return@supplyAsync UtilImageIO.loadImage(fisheyePath, file1) }

        return panoWritter(buffered0.get(), buffered1.get(), fisheyePath)
    }



    fun panoWritter(buffered0: BufferedImage, buffered1: BufferedImage, fisheyePath: String): BufferedImage {

        val imageType = ImageType.pl<GrayF32>(3, GrayF32::class.java)
        val interp = FactoryInterpolation.createPixel<Planar<GrayF32>>(0.0, 255.0, InterpolationType.BILINEAR,
                BorderType.ZERO, imageType)
        val distort = FactoryDistort.distort<Planar<GrayF32>, Planar<GrayF32>>(false, interp, imageType)
        val alg = MultiCameraToEquirectangular(distort, 1280, 640, imageType)
        alg.maskToleranceAngle = UtilAngle.radian(0.1f)
        val adjR = ConvertRotation3D_F32.eulerToMatrix(EulerType.XYZ, GrlConstants.F_PI / 2, 0f, 0f, null)

        val f2b = ConvertRotation3D_F32.eulerToMatrix(EulerType.ZYX, GrlConstants.F_PI, 0f, 0f, null)

        // Lens 1
        CompletableFuture.supplyAsync {
            val model0 = CalibrationIO.load<CameraUniversalOmni>(File(fisheyePath, "front.yaml"))
            val distort0 = LensDistortionUniversalOmni(model0)
            val mask0 = createMask(model0, distort0, UtilAngle.radian(182f).toDouble()) // camera has a known FOV of 185 degrees
            val frontToBack = Se3_F32()
            CommonOps_FDRM.mult(f2b, adjR, frontToBack.R)
            return@supplyAsync alg.addCamera(frontToBack, distort0, mask0)

        }

        //Lens 2
        val model1 = CalibrationIO.load<CameraUniversalOmni>(File(fisheyePath, "back.yaml"))
        val distort1 = LensDistortionUniversalOmni(model1)
        val mask1 = createMask(model1, distort1, UtilAngle.radian(182f).toDouble()) // the edges are likely to be noisy,
        val frontToFront = Se3_F32()
        frontToFront.rotation = adjR
        alg.addCamera(frontToFront, distort1, mask1)


        val images = ArrayList<Planar<GrayF32>>()

        // add the camera and specify which pixels are valid.  These functions precompute the entire transform
        // and can be relatively slow, but generating the equirectangular image should be much faster
        CompletableFuture.supplyAsync {
        val fisheye0 = ConvertBufferedImage.convertFrom<Planar<GrayF32>>(
                buffered0, true, ImageType.pl<GrayF32>(3, GrayF32::class.java))
            images.add(fisheye0)
        }
        val fisheye1 = ConvertBufferedImage.convertFrom<Planar<GrayF32>>(
                buffered1, true, ImageType.pl<GrayF32>(3, GrayF32::class.java))
        images.add(fisheye1)

        alg.render(images)

        return ConvertBufferedImage.convertTo(alg.renderedImage, null, true)
        //ShowImages.showWindow(equiOut,"Dual Fisheye to Equirectangular",true);
    }
}