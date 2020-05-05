package net.osmand.plus.viewangletool.command;

import net.osmand.plus.viewangletool.ViewAngleToolLayer;

public class SetLeftLimitCommand extends ViewAngleModeCommand {


	public SetLeftLimitCommand(ViewAngleToolLayer viewAngleToolLayer) {
		super(viewAngleToolLayer);
	}

	@Override
	public boolean execute() {
		getViewAngleLayer().setLeftLimitCenter();
		//getViewAngleLayer().checkViewAngleMode();
		return true;
	}

}
