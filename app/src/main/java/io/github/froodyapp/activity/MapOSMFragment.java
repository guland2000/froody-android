package io.github.froodyapp.activity;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.hsr.geohash.GeoHash;
import io.github.froodyapp.App;
import io.github.froodyapp.R;
import io.github.froodyapp.location.EntryMarker;
import io.github.froodyapp.location.MapListenerNotifier;
import io.github.froodyapp.location.RadiusMarkerClusterWithClusterClick;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.ui.BaseFragment;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.BlockCache;
import io.github.froodyapp.util.Helpers;

public class MapOSMFragment extends BaseFragment implements MapListener {
    //#####################
    //##      Statics
    //#####################
    public static final int ZOOMLEVEL_REQUEST_TRESHOLD_TO_4 = 8;
    public static final int ZOOMLEVEL_BLOCK4_TRESHOLD = 9;
    public static final int ZOOMLEVEL_BLOCK5_TRESHOLD = 13; // < this = Block4
    public static final String FRAGMENT_TAG = "MapOSMFragment";

    public static MapOSMFragment newInstance() {
        return new MapOSMFragment();
    }

    @BindView(R.id.maposm__fragment__map)
    public MapView map;

    //#####################
    //##      Members
    //#####################
    private IMapController mapController;
    private RotationGestureOverlay rotationGesture;
    private RadiusMarkerClusterWithClusterClick mapCluster;
    private ArrayList<EntryMarker> entryMarkersInCluster;
    private AppSettings appSettings;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maposm__fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Context c = getContext();
        getActivity().setTitle(R.string.app_name);
        prepareMap(c, map);
    }

    private void prepareMap(Context c, MapView map) {
        if (map == null) {
            App.log(getClass(), "Error: MapView NULL");
            return;
        }

        // Init
        appSettings = new AppSettings(c);
        mapController = map.getController();
        entryMarkersInCluster = new ArrayList<>();
        mapCluster = new RadiusMarkerClusterWithClusterClick(c);

        // Basic Options
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setTilesScaledToDpi(true);
        map.setMultiTouchControls(true);
        map.setMapListener(this);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(2);
        //map.setMinZoomLevel(ZOOMLEVEL_BLOCK4_TRESHOLD);

        // Enable rotation gesture
        rotationGesture = new RotationGestureOverlay(map);
        rotationGesture.setEnabled(false);
        map.getOverlays().add(rotationGesture);

        // Cluster
        Drawable clusterIconD = Helpers.getDrawableFromRes(c, R.drawable.green_circle);
        mapCluster.setIcon(((BitmapDrawable) clusterIconD).getBitmap());

        // Load entries with existing management code
        //App app = (App) (getActivity().getApplication());
        //List<FroodyEntryPlus> myEntries = new MyEntriesHelper(c).getMyEntries();
        //addFroodyEntriesToCluster(myEntries);

        // Load entries from cache
        loadEntriesFromBlockCache();
        recluster();

        tryZoomToLastMapLocation();
    }

    // Load position from last movement on map
    public void tryZoomToLastMapLocation() {
        if (appSettings.hasLastMapLocation()) {
            zoomToPosition(
                    appSettings.getLastMapLocationLatitude(),
                    appSettings.getLastMapLocationLongitude(),
                    appSettings.getLastMapLocationZoom()
            );
        } else {
            zoomToPosition(48.271165, 13.094350, 3);
        }
    }

    public GeoHash getMapCenterAsGeohash(int precision) {
        return GeoHash.withCharacterPrecision(map.getMapCenter().getLatitude(),
                map.getMapCenter().getLongitude(), precision);
    }

    public void loadEntriesFromBlockCache() {
        BlockCache blockCache = BlockCache.getInstance();
        List<FroodyEntryPlus> entries = blockCache.getAllCachedEntries();
        addFroodyEntriesToCluster(entries);
    }

    public void setRotationGestureEnabled(boolean enable) {
        rotationGesture.setEnabled(enable);
    }

    public int getCurrentZoomLevel() {
        return map.getZoomLevel();
    }

    public void addFroodyEntriesToCluster(List<FroodyEntryPlus> entries) {
        if (entries != null && map != null) {
            for (FroodyEntryPlus entry : entries) {
                addOrUpdateFroodyEntryToCluster(entry, false);
            }
            recluster();
        }
    }

    public void clearEntries() {
        entryMarkersInCluster.clear();
    }

    public void addOrUpdateFroodyEntryToCluster(FroodyEntryPlus entry, boolean autoRecluster) {
        EntryMarker marker = new EntryMarker(map, entry);

        // Remove + Insert then = Update/replace marker
        if (entryMarkersInCluster.contains(marker)) {
            entryMarkersInCluster.remove(marker);
        }

        if (!entry.getWasDeleted()) {
            entryMarkersInCluster.add(marker);
            mapCluster.add(marker);
        }

        if (autoRecluster) {
            recluster();
        }
    }

    public void removeFroodyEntryFromCluster(FroodyEntryPlus entry) {
        EntryMarker compareMarker = EntryMarker.from(map, entry);
        if (entryMarkersInCluster.contains(compareMarker)) {
            entryMarkersInCluster.remove(compareMarker);
            recluster();
        }
    }

    public void recluster() {
        map.post(new Runnable() {
            @Override
            public void run() {

                RadiusMarkerClusterWithClusterClick newCluster = new RadiusMarkerClusterWithClusterClick(map.getContext());
                for (EntryMarker marker : entryMarkersInCluster) {
                    newCluster.add(marker);
                }
                map.getOverlays().remove(mapCluster);
                map.invalidate();
                mapCluster = newCluster;
                map.getOverlays().add(mapCluster);
                map.invalidate();
            }
        });
    }

    /**
     * Zoom to given position with default zoom level
     *
     * @param latitude  Latitude
     * @param longitude Longitude
     * @return if zoom was successful
     */
    public boolean zoomToPosition(double latitude, double longitude) {
        return zoomToPosition(latitude, longitude, 16);
    }

    /**
     * Zoom to given position with zoom level
     *
     * @param latitude  Latitude
     * @param longitude Longitude
     * @param zoom      Zoom level
     * @return if zoom was successful
     */
    public boolean zoomToPosition(double latitude, double longitude, int zoom) {
        if (mapController != null) {
            mapController.setZoom(zoom);
            mapController.setCenter(new GeoPoint(latitude, longitude));
            return true;
        }
        return false;
    }

    /**
     * Zooms to given bounding box
     *
     * @param bbox The bounding box
     */
    public void zoomToBoundingBox(final BoundingBox bbox) {
        if (map != null && bbox != null) {
            map.postDelayed(new Runnable() {
                public void run() {
                    map.zoomToBoundingBox(bbox, false);  //Bug: https://github.com/osmdroid/osmdroid/issues/236#issuecomment-257061439
                    map.zoomToBoundingBox(bbox, false);  // Call Twice and without animation because of bug.
                }
            }, 100);
        }
    }

    /*
        #
        #   Some special positions
        #
     */

    /**
     * Zooms to austria
     */
    public void zoomToAustria() {
        if (map != null) {
            List<GeoPoint> boxPoints = new ArrayList<>();
            boxPoints.add(new GeoPoint(47.254028, 9.399102));
            boxPoints.add(new GeoPoint(46.532729, 14.053614));
            boxPoints.add(new GeoPoint(48.035731, 17.365241));
            boxPoints.add(new GeoPoint(49.197737, 15.266852));

            BoundingBox bbox = BoundingBox.fromGeoPoints(boxPoints);
            zoomToBoundingBox(bbox);
        }
    }

    public void zoomToHgb() {
        zoomToPosition(48.368399, 14.513167);
    }

    @Override
    public boolean onScroll(ScrollEvent scrollEvent) {
        new MapListenerNotifier(map).start();
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent zoomEvent) {
        new MapListenerNotifier(map).start();
        App.log(getClass(), zoomEvent.getZoomLevel() + "");
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map__fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(R.string.app_name);
        activity.navigationView.setCheckedItem(R.id.nav_map);

    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
