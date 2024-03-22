import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

final class MainForm extends JFrame {
    private JPanel mainPanel;
    private JButton calculatePercentageButton;
    private JTextArea textArea1;
    private JLabel percentageLabel;

    MainForm() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setTitle("Double Project");
        this.setMinimumSize(new Dimension(800, 600));
        this.add(mainPanel);
        this.pack();
        calculatePercentageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dictionary d = new Dictionary();
                String text = textArea1.getText();
                if (text != null) {
                    percentageLabel.setText(Float.toString(d.calcPosteriorSpamProbability(text) * 100));
                }
            }
        });
    }

    public static void main(String[] args) {
        new MainForm();
    }
}
