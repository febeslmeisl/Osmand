package net.osmand.plus.viewangletool.command;

import net.osmand.GPXUtilities.WptPt;
import net.osmand.data.LatLon;
import net.osmand.plus.viewangletool.ViewAngleToolLayer;
import net.osmand.plus.viewangletool.command.ViewAngleModeCommand;

public class SetViewPositionCommand extends ViewAngleModeCommand {


	public SetViewPositionCommand(ViewAngleToolLayer viewAngleToolLayer) {
		super(viewAngleToolLayer);
	}

	@Override
	public boolean execute() {
		getViewAngleLayer().setViewPositionCenter();
		//getViewAngleLayer().checkViewAngleMode();
		return true;
	}

}
