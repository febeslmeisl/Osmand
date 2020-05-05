package net.osmand.plus.viewangletool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextPaint;

import net.osmand.Location;
import net.osmand.PlatformUtil;
import net.osmand.data.LatLon;
import net.osmand.data.QuadPoint;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.OsmandMapLayer;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.util.MapUtils;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

public class ViewAngleToolLayer extends OsmandMapLayer {

	private static final double FAR_VIEW_DISTANCE = 150000.0; // visual Range in meters
	//private static final double VIEW_RHUMB_STEP = 500; //Path Point steps

	private static final int DISTANCE_PADDING = 2;
	private static final int DISTANCE_TEXT_SIZE = 11;
	private static final int ICON_SIZE = 18;

	private static final Log LOG = PlatformUtil.getLog(ViewAngleToolLayer.class.getName());

	private OsmandMapTileView view;
	boolean isInViewAngleMode;
	private Bitmap centerIconDay;
	private Bitmap centerIconNight;
	private Drawable pointIcon;
	private final RenderingLineAttributes viewAttrs = new RenderingLineAttributes("viewAngleTool");
	private int marginPointIconX;
	private int marginPointIconY;
	private final Path path = new Path();
	private final List<Float> tx = new ArrayList<>();
	private final List<Float> ty = new ArrayList<>();
	private float textSize;
	private float scale = 1.0f;

	private LatLon viewPosition = null;
	private LatLon leftLimit = null;
	private LatLon rightLimit = null;
	private MapActivity mapActivity;
	private Paint mainPaint;
	private Paint textPaint;


	public ViewAngleToolLayer(MapActivity mapActivity)
	{
		this.mapActivity = mapActivity;
	}

	@Override
	public void initLayer(OsmandMapTileView view) {
		this.view = view;

		isInViewAngleMode = false;

		scale = view.getResources().getDisplayMetrics().density;

		centerIconDay = BitmapFactory.decodeResource(view.getResources(), R.drawable.map_ruler_center_day);
		centerIconNight = BitmapFactory.decodeResource(view.getResources(), R.drawable.map_ruler_center_night);

        pointIcon = view.getResources().getDrawable(R.drawable.map_view_angle_pos);

		mainPaint = new Paint();
		mainPaint.setAntiAlias(true);
		mainPaint.setDither(true);
		mainPaint.setFilterBitmap(true);

		textPaint=new TextPaint();
		textPaint.setAntiAlias(true);
		textPaint.setDither(true);
		textPaint.setFilterBitmap(true);



		marginPointIconY = Math.round(scale*ICON_SIZE / 2);
		marginPointIconX = Math.round(scale*ICON_SIZE / 2);

		textSize= scale*DISTANCE_TEXT_SIZE;

	}
	private void setIconColor(Drawable d, Paint p) {
		try {
			LayerDrawable ld = (LayerDrawable) d;
			GradientDrawable gd =  (GradientDrawable) ld.getDrawable(1);
			gd.setStroke(Math.round(p.getStrokeWidth()),p.getColor());
		} catch (Exception e) {
			LOG.error("An internal error occurred while setting color for view angle limit point drawable. Expecting a LayerDrawable containing GradientDrawable in layer index 1. Falling back to default color. Please check resource definition of Drawable.", e);
			//We have an issue with the resources, but we fall back to default Icon
		}
	}

	@Override
	public void onDraw(Canvas canvas, RotatedTileBox tb, DrawSettings settings) {
		if (isInViewAngleMode) {

			drawCenterIcon(canvas, tb, settings.isNightMode());

			LatLon viewPos = viewPosition;
			if (viewPos != null) {
				if (viewPos!=null) {
					int locX = tb.getPixXFromLonNoRot(viewPos.getLongitude());
					int locY = tb.getPixYFromLatNoRot(viewPos.getLatitude());
					viewAttrs.updatePaints(view.getApplication(), settings, tb);
                    setIconColor(pointIcon, viewAttrs.paint);
                    pointIcon.setBounds(locX - marginPointIconX, locY - marginPointIconY, locX + marginPointIconX, locY + marginPointIconY);
                    pointIcon.draw(canvas);
					LatLon centerLatLon = tb.getCenterLatLon();
					double distance = MapUtils.getDistance(viewPos.getLatitude(), viewPos.getLongitude(), centerLatLon.getLatitude(), centerLatLon.getLongitude());
					drawDistance(canvas, tb, (float) distance, settings.isNightMode());
					drawLimitLine(canvas, leftLimit, viewPos, pointIcon, viewAttrs.paint2, tb, settings);
					drawLimitLine(canvas, rightLimit, viewPos, pointIcon, viewAttrs.paint3, tb, settings);
				}
			}
		}
	}

	private void drawLimitLine(Canvas canvas, LatLon limit, LatLon viewPos, Drawable limitIcon, Paint paint, RotatedTileBox tb, DrawSettings settings) {
		if (limit!=null) {

			setIconColor(limitIcon, paint);
			int locX = tb.getPixXFromLonNoRot(limit.getLongitude());
			int locY = tb.getPixYFromLatNoRot(limit.getLatitude());
			limitIcon.setBounds(locX - marginPointIconX, locY - marginPointIconY, locX + marginPointIconX, locY + marginPointIconY);
			limitIcon.draw(canvas);
			float bearing = getLocationFromLL(viewPos.getLatitude(), viewPos.getLongitude()).bearingTo(getLocationFromLL(limit.getLatitude(), limit.getLongitude()));

			tx.clear();
			ty.clear();
			tx.add((float)tb.getPixXFromLonNoRot(viewPos.getLongitude()));
			ty.add((float)tb.getPixYFromLatNoRot(viewPos.getLatitude()));
			tx.add((float)tb.getPixXFromLonNoRot(limit.getLongitude()));
			ty.add((float)tb.getPixYFromLatNoRot(limit.getLatitude()));
			double dist=FAR_VIEW_DISTANCE;
			//for (double dist=VIEW_RHUMB_STEP; dist<=FAR_VIEW_DISTANCE; dist+=VIEW_RHUMB_STEP) {
			LatLon farLimit = MapUtils.rhumbDestinationPoint(viewPos, dist,bearing);
			tx.add((float)tb.getPixXFromLonNoRot(farLimit.getLongitude()));
			ty.add((float)tb.getPixYFromLatNoRot(farLimit.getLatitude()));
			//}
			path.reset();
			calculatePath(tb, tx, ty, path);
			canvas.drawPath(path, paint);
		}
	}

	private void drawDistance(Canvas canvas, RotatedTileBox tb, float distance, boolean nightMode) {
		String distStr = OsmAndFormatter.getFormattedDistance(distance, mapActivity.getMyApplication());
		int centerIconHeight;
		if (nightMode) {
			textPaint.setARGB(250, 255, 255, 255);
			centerIconHeight = centerIconNight.getHeight();
		} else  {
			textPaint.setARGB(230, 0, 0, 0);
			centerIconHeight = centerIconDay.getHeight();
		}
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(textSize);
		QuadPoint center = tb.getCenterPixelPoint();
		canvas.rotate(-tb.getRotate(), center.x, center.y);
		canvas.drawText(String.format("%1$s", distStr), center.x, center.y+centerIconHeight/2 +textSize+DISTANCE_PADDING, textPaint);
		canvas.rotate(tb.getRotate(), center.x, center.y);
	}

	private void drawCenterIcon(Canvas canvas, RotatedTileBox tb, boolean nightMode) {
		QuadPoint center =tb.getCenterPixelPoint();
		canvas.rotate(-tb.getRotate(), center.x, center.y);
		if (nightMode) {
			canvas.drawBitmap(centerIconNight, center.x - centerIconNight.getWidth() / 2,
					center.y - centerIconNight.getHeight() / 2, mainPaint);
		} else {
			canvas.drawBitmap(centerIconDay, center.x - centerIconDay.getWidth() / 2,
					center.y - centerIconDay.getHeight() / 2, mainPaint);
		}
		canvas.rotate(tb.getRotate(), center.x, center.y);
	}

	private LatLon getCenterPoint() {
		RotatedTileBox tb = view.getCurrentRotatedTileBox();
		LatLon l = tb.getLatLonFromPixel(tb.getCenterPixelX(), tb.getCenterPixelY());
		return l;
	}

	public void setViewPositionCenter() {
		setViewPosition(getCenterPoint());
	}

	public void setViewPosition(LatLon vp) {
		viewPosition = vp;
		redraw();
	}

	public void setLeftLimitCenter() {
		setLeftLimit(getCenterPoint());
	}

	public void setLeftLimit(LatLon ll) {
		leftLimit = ll;
		redraw();
	}

	public void setRightLimitCenter() {
		setRightLimit(getCenterPoint());
	}

	public void setRightLimit(LatLon rl) {
		rightLimit = rl;
		redraw();
	}

	private void redraw() {
		if (isInViewAngleMode) {
			view.refreshMap();
		}
	}

	public void setViewAngleMode() {
		isInViewAngleMode = true;
	}

	public void resetViewAngleMode() {
		isInViewAngleMode=false;
	}

	public boolean isInViewAngleMode() {
		return isInViewAngleMode;
	}

	public LatLon getViewPosition() {
		return viewPosition;
	}

	public LatLon getLeftLimit() {
		return leftLimit;
	}

	public LatLon getRightLimit() {
		return rightLimit;
	}

	private void moveMapToLatLon(double lat, double lon) {
		view.getAnimatedDraggingThread().startMoving(lat, lon, view.getZoom(), true);
	}

	public void refreshMap() {
		view.refreshMap();
	}

	@Override
	public void destroyLayer() {
		resetViewAngleMode();
	}

	@Override
	public boolean drawInScreenPixels() {
		return false;
	}

	private Location getLocationFromLL(double lat, double lon) {
		Location l = new Location("");
		l.setLatitude(lat);
		l.setLongitude(lon);
		return l;
	}

}
