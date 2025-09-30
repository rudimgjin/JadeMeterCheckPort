package com.jademeter.jmeter.plugin;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;

import java.awt.*;

public class PortCheckConfigGui extends AbstractConfigGui {
    private PortCheckPanel panel;

    public PortCheckConfigGui() {
        init();
    }

    @Override
    public String getStaticLabel() {
        return "Port Checker";
    }

    @Override
    public String getLabelResource() {
        return "";
    }

    @Override
    public TestElement createTestElement() {
        TestElement el = new org.apache.jmeter.config.ConfigTestElement();
        modifyTestElement(el);
        return el;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        panel = new PortCheckPanel();
        add(panel, BorderLayout.CENTER);
    }

    @Override
    public void clearGui() {
        super.clearGui();
    }
}
