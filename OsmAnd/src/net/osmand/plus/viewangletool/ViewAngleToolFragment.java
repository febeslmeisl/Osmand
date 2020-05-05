package net.osmand.plus.viewangletool;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import net.osmand.AndroidUtils;
import net.osmand.data.LatLon;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.BaseOsmAndFragment;
import net.osmand.plus.helpers.AndroidUiHelper;
import net.osmand.plus.viewangletool.command.SetLeftLimitCommand;
import net.osmand.plus.viewangletool.command.SetRightLimitCommand;
import net.osmand.plus.viewangletool.command.SetViewPositionCommand;


public class ViewAngleToolFragment extends BaseOsmAndFragment {

	public static final String TAG = "ViewAngleToolFragment";

	private View mainView;

	private boolean nightMode;
	private int cachedMapPosition;

	private static final String VIEW_POS_BUNDLE_KEY = ViewAngleToolFragment.class.getCanonicalName() + ".VIEW_POS_BUNDLE_KEY";
	private static final String LEFT_LIMIT_BUNDLE_KEY = ViewAngleToolFragment.class.getCanonicalName() + ".LEFT_LIMIT_BUNDLE_KEY";
	private static final String RIGHT_LIMIT_BUNDLE_KEY = ViewAngleToolFragment.class.getCanonicalName() + ".RIGHT_LIMIT_BUNDLE_KEY";


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final MapActivity mapActivity = (MapActivity) getActivity();
		final ViewAngleToolLayer viewAngleLayer = mapActivity.getMapLayers().getViewAngleToolLayer();


		nightMode = mapActivity.getMyApplication().getDaynightHelper().isNightModeForMapControls();


		View view = UiUtilities.getInflater(getContext(), nightMode).inflate(R.layout.fragment_viewangle_tool, null);

		mainView = view.findViewById(R.id.main_view);
		AndroidUtils.setBackground(mapActivity, mainView, nightMode, R.drawable.bg_bottom_menu_light, R.drawable.bg_bottom_menu_dark);


		mainView.findViewById(R.id.set_position_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addViewPositionCenter();
			}
		});

		mainView.findViewById(R.id.set_left_limit_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addLeftLimitCenter();
			}
		});

		mainView.findViewById(R.id.set_right_limit_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addRightLimitCenter();
			}
		});
		ViewAngleToolLayer vatl = getViewAngleLayer();
		if (savedInstanceState != null) {
			vatl.setViewPosition((LatLon) savedInstanceState.getSerializable(VIEW_POS_BUNDLE_KEY));
			vatl.setLeftLimit((LatLon)savedInstanceState.getSerializable(LEFT_LIMIT_BUNDLE_KEY));
			vatl.setRightLimit((LatLon) savedInstanceState.getSerializable(RIGHT_LIMIT_BUNDLE_KEY));
		}
		vatl.setViewAngleMode();
		return view;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		ViewAngleToolLayer vatl = getViewAngleLayer();
		outState.putSerializable(VIEW_POS_BUNDLE_KEY, vatl.getViewPosition());
		outState.putSerializable(LEFT_LIMIT_BUNDLE_KEY, vatl.getLeftLimit());
		outState.putSerializable(RIGHT_LIMIT_BUNDLE_KEY, vatl.getRightLimit());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		getMapActivity().getMapLayers().getMapControlsLayer().showMapControlsIfHidden();
		cachedMapPosition = getMapActivity().getMapView().getMapPosition();
		setDefaultMapPosition();
		getViewAngleLayer().setViewAngleMode();
	}

	@Override
	public void onPause() {
		super.onPause();
		setMapPosition(cachedMapPosition);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getViewAngleLayer().resetViewAngleMode();
	}

	@Override
	public int getStatusBarColorId() {
		return R.color.status_bar_transparent_gradient;
	}

	private MapActivity getMapActivity() {
		return (MapActivity) getActivity();
	}

	private ViewAngleToolLayer getViewAngleLayer() {
		MapActivity mapActivity = getMapActivity();
		if (mapActivity != null) {
			return mapActivity.getMapLayers().getViewAngleToolLayer();
		}
		return null;
	}


	@Override
	protected Drawable getContentIcon(@DrawableRes int id) {
		return getIcon(id, nightMode ? R.color.icon_color_default_dark : R.color.icon_color_default_light);
	}


	private boolean addViewPositionCenter() {
		return new SetViewPositionCommand(getViewAngleLayer()).execute();
	}

	private boolean addLeftLimitCenter() {
		return new SetLeftLimitCommand(getViewAngleLayer()).execute();
	}

	private boolean addRightLimitCenter() {
		return new SetRightLimitCommand(getViewAngleLayer()).execute();
	}

	private void setDefaultMapPosition() {
		setMapPosition(OsmandSettings.CENTER_CONSTANT);
	}

	private void setMapPosition(int position) {
		MapActivity mapActivity = getMapActivity();
		if (mapActivity != null) {
			mapActivity.getMapView().setMapPosition(position);
			mapActivity.refreshMap();
		}
	}

	public void quit() {
		getViewAngleLayer().resetViewAngleMode();
		MapActivity  mapActivity=getMapActivity();
		if (mapActivity!=null) {
			mapActivity.getSupportFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
		}
	}


	public static boolean showInstance(FragmentManager fragmentManager) {
		return showFragment(new ViewAngleToolFragment(), fragmentManager);
	}

	private static boolean showFragment(ViewAngleToolFragment fragment, FragmentManager fragmentManager) {
		try {
			fragment.setRetainInstance(true);
			fragmentManager.beginTransaction()
					.add(R.id.bottomFragmentContainer, fragment, ViewAngleToolFragment.TAG)
					.commitAllowingStateLoss();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
