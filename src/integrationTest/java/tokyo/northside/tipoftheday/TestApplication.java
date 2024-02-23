package tokyo.northside.tipoftheday;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import tokyo.northside.tipoftheday.tips.DefaultTip;
import tokyo.northside.tipoftheday.tips.DefaultTipOfTheDayModel;
import tokyo.northside.tipoftheday.tips.Tip;
import tokyo.northside.tipoftheday.tips.TipOfTheDayModel;

public class TestApplication extends JFrame {

    private TipOfTheDay totd1;

    public TestApplication() throws HeadlessException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createTipOfTheDay();
        setTitle("Tip of the Day");
        setPreferredSize(new Dimension(950, 500));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static final String TIP1_NAME = "Plain Text Tip";
    public static final String TIP2_NAME = "HTML Text Tip";
    public static final String TIP1 = "This is the first tip " + "This is the first tip "
            + "This is the first tip " + "This is the first tip " + "This is the first tip "
            + "This is the first tip\n" + "This is the first tip " + "This is the first tip";
    public static final String TIP2 = "<html><head></head><body>This is an html <b>TIP</b><br><center>"
            + "<table border=\"1\">" + "<tr><td>1</td><td>entry 1</td></tr>"
            + "<tr><td>2</td><td>entry 2</td></tr>" + "<tr><td>3</td><td>entry 3</td></tr>"
            + "</table></center></body></html>";

    private TipOfTheDayModel createTipOfTheDayModel() {
        List<Tip> tips = new ArrayList<>();
        tips.add(new DefaultTip(TIP1_NAME, TIP1));
        tips.add(DefaultTip.of(TIP2_NAME, TIP2));
        return new DefaultTipOfTheDayModel(tips);
    }

    private void createTipOfTheDay() {
        // model of "tip of the day" component.
        TipOfTheDayModel model = createTipOfTheDayModel();
        JPanel panel = new JPanel();
        totd1 = new TipOfTheDay(model);
        totd1.setPreferredSize(new Dimension(900, 450));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(totd1);
        add(panel);

        JPanel buttonPanel = new JPanel();
        JButton previousTipButton = new JButton();
        previousTipButton.setText("Previous tip(F1)<<");
        previousTipButton.setName("PreviousButton");
        buttonPanel.add(previousTipButton);
        JButton nextTipButton = new JButton();
        nextTipButton.setText(">>Next tip(F2)");
        nextTipButton.setName("NextButton");
        buttonPanel.add(nextTipButton);

        JButton closeButton = new JButton();
        closeButton.setText("Close(ctrl Q)");
        closeButton.setName("CloseButton");
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        previousTipButton.addActionListener(e -> totd1.previousTip());
        nextTipButton.addActionListener(e -> totd1.nextTip());
        closeButton.addActionListener(actionEvent -> {
            if (isDisplayable()) {
                dispose();
            }
        });

        panel.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDisplayable()) {
                    dispose();
                }
            }
        });
        // set keymap for actions of TipOfTheDay
        totd1.getInputMap().put(KeyStroke.getKeyStroke("F1"), "previousTip");
        totd1.getInputMap().put(KeyStroke.getKeyStroke("F2"), "nextTip");
        // set keymap for our actions.
        panel.getInputMap().put(KeyStroke.getKeyStroke("ctrl Q"), "close");
    }
}
