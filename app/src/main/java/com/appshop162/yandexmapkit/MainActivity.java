package com.appshop162.yandexmapkit;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LongTapDialog.LongTapListener, DrivingSession.DrivingRouteListener{

    private MapView mapView;
    private TextView routePanelCollapser;
    private TextView tvLatA, tvLonA, tvLatB, tvLonB;
    private Button buttonBuildRoute;
    private LinearLayout routePanel;
    private Point TARGET_LOCATION = new Point(55.751574, 37.573856);
    private Map map;
    private InputListener inputListener;
    private boolean panelIsCollapsed = false;
    private double latA, lonA, latB, lonB, latCurrent, lonCurrent;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(getString(R.string.api_key));
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        mapView = (MapView) findViewById(R.id.map_view);
        routePanel = (LinearLayout) findViewById(R.id.route_panel);
        routePanelCollapser = (TextView) findViewById(R.id.panel_collapser);
        tvLatA = (TextView) findViewById(R.id.lat_a);
        tvLonA = (TextView) findViewById(R.id.lon_a);
        tvLatB = (TextView) findViewById(R.id.lat_b);
        tvLonB = (TextView) findViewById(R.id.lon_b);
        buttonBuildRoute = (Button) findViewById(R.id.button_build_route);

        collapsePanel();

        buttonBuildRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildRoute(new Point(latA, lonA), new Point(latB, lonB));
            }
        });

        routePanelCollapser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                panelIsCollapsed = !panelIsCollapsed;
                collapsePanel();
            }
        });

        map = mapView.getMap();
        mapObjects = map.getMapObjects().addCollection();
        inputListener = new InputListener() {
            @Override
            public void onMapTap(@NonNull Map map, @NonNull Point point) {
                System.out.println(point.getLatitude() + "   " + point.getLongitude());
            }

            @Override
            public void onMapLongTap(@NonNull Map map, @NonNull Point point) {
                LongTapDialog longTapDialog = new LongTapDialog();
                latCurrent = point.getLatitude();
                lonCurrent = point.getLongitude();
                longTapDialog.show(getSupportFragmentManager(), "TAG");

                PlacemarkMapObject mark = mapView.getMap().getMapObjects().addPlacemark(point);
                mark.setOpacity(0.5f);
                mark.setIcon(ImageProvider.fromResource(MainActivity.this, R.drawable.mark));
            }
        };

        map.move(new CameraPosition(TARGET_LOCATION, 10.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0), null);
        map.addInputListener(inputListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    private void collapsePanel() {
        if (panelIsCollapsed) {
            routePanel.setVisibility(View.GONE);
            routePanelCollapser.setText(R.string.down_triangle);
        } else {
            routePanel.setVisibility(View.VISIBLE);
            routePanelCollapser.setText(R.string.up_triangle);
        }
    }

    public void setACoordinates() {
        latA = latCurrent;
        lonA = lonCurrent;
        tvLatA.setText(Double.toString(latA));
        tvLonA.setText(Double.toString(lonA));
        if (!(tvLatB.getText().equals("") || tvLonB.getText().equals(""))) buttonBuildRoute.setVisibility(View.VISIBLE);
    }

    public void setBCoordinates() {
        latB = latCurrent;
        lonB = lonCurrent;
        tvLatB.setText(Double.toString(latB));
        tvLonB.setText(Double.toString(lonB));
        if (!(tvLatA.getText().equals("") || tvLonA.getText().equals(""))) buttonBuildRoute.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAClick(DialogFragment dialog) {
        setACoordinates();
    }

    @Override
    public void onBClick(DialogFragment dialog) {
        setBCoordinates();
    }

    @Override
    public void onCancel(DialogFragment dialog) {
        //
    }

    private void buildRoute(Point pointA, Point pointB) {
        Point SCREEN_CENTER = new Point(
                (pointA.getLatitude() + pointB.getLatitude()) / 2,
                (pointA.getLongitude() + pointB.getLongitude()) / 2);

        map.move(new CameraPosition(SCREEN_CENTER, 5, 0, 0));
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();

        DrivingOptions options = new DrivingOptions();
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(pointA, RequestPointType.WAYPOINT,null));
        requestPoints.add(new RequestPoint(pointB, RequestPointType.WAYPOINT,null));
        drivingSession = drivingRouter.requestRoutes(requestPoints, options, this);
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
        mapObjects.addPolyline(routes.get(0).getGeometry());
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void submitRequest() {
    }
}
