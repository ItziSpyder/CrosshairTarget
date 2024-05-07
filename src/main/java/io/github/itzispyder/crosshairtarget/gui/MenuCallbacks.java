package io.github.itzispyder.crosshairtarget.gui;

import io.github.itzispyder.improperui.script.CallbackHandler;
import io.github.itzispyder.improperui.script.CallbackListener;
import io.github.itzispyder.improperui.script.events.MouseEvent;
import net.minecraft.util.Util;

public class MenuCallbacks implements CallbackListener {

    @CallbackHandler
    public void handleMouseCallbacks(MouseEvent e) {
        switch (e.input) {
            case CLICK -> onClick(e);
            case RELEASE -> onRelease(e);
        }
    }

    public void onClick(MouseEvent e) {
        if ("improperui-ad".equals(e.target.getId())) {
            Util.getOperatingSystem().open("https://github.com/itzispyder/improperui");
        }
    }

    public void onRelease(MouseEvent e) {

    }
}
