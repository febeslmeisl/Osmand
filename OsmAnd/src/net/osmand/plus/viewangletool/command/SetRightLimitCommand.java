package net.osmand.plus.viewangletool.command;

import net.osmand.plus.viewangletool.ViewAngleToolLayer;

public class SetRightLimitCommand extends ViewAngleModeCommand {


	public SetRightLimitCommand(ViewAngleToolLayer viewAngleToolLayer) {
		super(viewAngleToolLayer);
	}

	@Override
	public boolean execute() {
		getViewAngleLayer().setRightLimitCenter();
		//getViewAngleLayer().checkViewAngleMode();
		return true;
	}

}
