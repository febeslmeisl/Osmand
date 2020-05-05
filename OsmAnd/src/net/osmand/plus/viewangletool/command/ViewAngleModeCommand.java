package net.osmand.plus.viewangletool.command;

import net.osmand.plus.measurementtool.MeasurementToolLayer;
import net.osmand.plus.viewangletool.ViewAngleToolLayer;

public abstract class ViewAngleModeCommand implements Command {

	private ViewAngleToolLayer viewAngleLayer;

	protected ViewAngleModeCommand(ViewAngleToolLayer viewAngleLayer) {
		this.viewAngleLayer = viewAngleLayer;
	}

	public ViewAngleToolLayer getViewAngleLayer() {
		return viewAngleLayer;
	}


}
