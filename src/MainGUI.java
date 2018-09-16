import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;


public class MainGUI {

    private static final Pano currentPano = new PanoBoofCvImpl();

    private static final ImgResizer resimg = new ImgResizer();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createGUI();
            }
        });
    }

    //settings for UI
    protected static void createGUI() {
        final JFrame frame = new JFrame("for360img");

        final DefaultListModel listModel = new DefaultListModel();

        GridBagConstraints c = new GridBagConstraints();

        final JList list = new JList(listModel);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == 127) {
                    int[] selected = list.getSelectedIndices();
                    for(int i = selected.length - 1; i >= 0; i--) {
                        listModel.remove(selected[i]);
                    }
                }
            }
        });

        JScrollPane scrol = new JScrollPane(list);
        scrol.setPreferredSize(new Dimension(300, 180));

        JButton addbuton = new JButton("Add");
        JButton removebutton = new JButton("Delete");
        addbuton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                fileopen.setMultiSelectionEnabled(true);
                fileopen.setApproveButtonText("Выбрать файл");
                int ret = fileopen.showDialog(null, "Выбрать файл");
                if(ret == JFileChooser.APPROVE_OPTION) {
                    File[] file = fileopen.getSelectedFiles();
                    for(int i = 0; i < file.length; i++) {
                        listModel.addElement(file[i].getPath());
                    }
                }
            }
        });
        removebutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selected = list.getSelectedIndices();
                for(int i = selected.length - 1; i >= 0; i--) {
                    listModel.remove(selected[i]);
                }
            }
        });


        JPanel filepanel = new JPanel();
        filepanel.setLayout(new GridBagLayout());
        c.gridwidth = 2;
        c.weightx = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.fill =GridBagConstraints.BOTH;
        filepanel.add(scrol, c);
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 1;
        filepanel.add(addbuton, c);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        filepanel.add(removebutton, c);


        final JTextField text = new JTextField("Destination directory",15);
        text.setFont(new Font(null, Font.BOLD, 12));
        text.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if(text.getText().equals("Destination directory"))
                    text.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(text.getText().equals(""))
                    text.setText("Destination directory");
            }
        });
        JButton dirbutton = new JButton("Select");// + new ImageIcon("img2.png"));
        dirbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileopen.setApproveButtonText("Select directory");
                int ret = fileopen.showDialog(null, "Select directory");
                if(ret == JFileChooser.APPROVE_OPTION) {
                    File dir = fileopen.getSelectedFile();
                    text.setText(dir.getPath());
                }
            }
        });

        JPanel dirpanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.RELATIVE;
        dirpanel.add(text,c);
        c.gridx = 1;
        c.gridy = 0;
        dirpanel.add(dirbutton);


        ButtonGroup group = new ButtonGroup();
        final JRadioButton large = new JRadioButton("4096 x 4096");
        final JRadioButton medium = new JRadioButton("2048 x 2048");
        final JRadioButton small = new JRadioButton("3888 x 3888");
        small.setSelected(true);

        group.add(large);
        group.add(small);
        group.add(medium);

        final JRadioButton another = new JRadioButton("Other");

        group.add(another);
        final JTextField size1 = new JTextField(4);
        size1.setEditable(false);

        JLabel label = new JLabel("x");
        final JTextField size2 = new JTextField(4);
        size1.setFont(new Font(null, Font.BOLD, 12));
        size2.setFont(new Font(null, Font.BOLD, 12));

        size2.setEditable(false);
        size2.setBackground(Color.WHITE);
        size2.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        size1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        size1.setBackground(Color.WHITE);
        size1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if ((c < '0') || (c > '9')) {
                    e.consume();
                } else {
                    size2.setText(size2.getText() + c);
                }
            }
        });
        another.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(another.isSelected())

                    size1.setEditable(true);
                else size1.setEditable(false);
            }
        });
        small.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                size1.setEditable(false);
            }
        });
        medium.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                size1.setEditable(false);
            }
        });
        large.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                size1.setEditable(false);
            }
        });
        size1.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if ((e.getKeyCode() == 127) || (e.getKeyCode() == 8))
                    size2.setText(size1.getText());
            }
        });

        JPanel textpanel = new JPanel();
        textpanel.add(size1);
        textpanel.add(label);
        textpanel.add(size2);

        JPanel format = new JPanel(new GridBagLayout());
        format.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Image resolution"));
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        format.add(large, c);
        c.gridy = 1;
        format.add(medium, c);
        c.gridy = 2;
        format.add(small, c);
        c.gridy = 3;
        format.add(another, c);
        c.gridy = 4;
        format.add(textpanel, c);


        JButton sub = new JButton("Execute");
        sub.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //files
                String[] files = new String[listModel.getSize()];
                for(int i = 0; i < listModel.getSize(); i++) {
                    files[i] = (String)listModel.getElementAt(i);
                }
                boolean boolPath = false;
                if((text.getText().equals("")) || (text.getText().equals("Destination directory")))
                    boolPath = false;
                else
                    boolPath = true;

                //format
                int resol = 3888;
                if(small.isSelected())
                    resol = 3888;
                if(medium.isSelected())
                    resol = 2048;
                if(large.isSelected())
                    resol = 4096;
                if(another.isSelected()) {
                    if(size1.getText().equals(""))
                        JOptionPane.showMessageDialog(frame,"Enter image resolution", "Warning", JOptionPane.WARNING_MESSAGE);
                    else
                        resol = Integer.parseInt(size1.getText());
                }

                String dir;

                PanoImages panoImages;

                for(int i = 0; i < files.length; i++) {
                    if (boolPath == false) {
                        dir = resimg.getPath(files[i]);
                    } else {
                        dir = text.getText();
                    }
                    panoImages = resimg.resizerImg(files[i], resol, dir);

                    BufferedImage pano = currentPano.panoWritter(
                            panoImages.getLeftImage(),
                            panoImages.getRightImage(),
                            dir
                    );
                    resimg.saveImg(pano, "jpg", dir + resimg.getName(files[i]) + "_pano.jpg" );
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill =GridBagConstraints.BOTH;
        panel.add(filepanel, c);
        c.gridheight = 1;
        c.gridy = 0;
        c.gridx = 1;
        panel.add(dirpanel, c);
        c.gridheight = 1;
        c.gridx = 1;
        c.gridy = 1;
        panel.add(format, c);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.SOUTHEAST;
        panel.add(sub, c);

        DragListener d = new DragListener(listModel);
        new DropTarget(panel, d);

        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.darkGray);
        frame.setForeground(Color.darkGray);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }
}