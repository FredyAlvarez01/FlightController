package com.alvarez.edu.flightmodeapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alvarez.edu.flightmodeapp.postura.AttitudeIndicator;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.util.Utils;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainApm extends AppCompatActivity
implements NavigationView.OnNavigationItemSelectedListener ,
           View.OnClickListener,DroneListener, TowerListener,
           ServiceConnection, GoogleApiClientManager.ManagerListener,OnMapReadyCallback,
           SeekBar.OnSeekBarChangeListener
            {


       GoogleMap mMap;

    private static final String EXTERNAL_FILENAME = "pruebaE.txt";


    private final Handler handler = new Handler();
    private DroidPlannerPrefs dpPrefs;
    private ControlTower controlTower;


    private com.o3dr.android.client.Drone drone;

    public static final String ACTION_TOGGLE_DRONE_CONNECTION = Utils.PACKAGE_NAME
            + ".ACTION_TOGGLE_DRONE_CONNECTION";
    public static final String EXTRA_ESTABLISH_CONNECTION = "extra_establish_connection";

    private static final int FOLLOW_LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private static final long FOLLOW_LOCATION_UPDATE_INTERVAL = 30000; // ms
    private static final long FOLLOW_LOCATION_UPDATE_FASTEST_INTERVAL = 5000; // ms
    private static final float FOLLOW_LOCATION_UPDATE_MIN_DISPLACEMENT = 0; // m


    public static final String ACTION_DRONE_CONNECTION_FAILED = Utils.PACKAGE_NAME
            + ".ACTION_DRONE_CONNECTION_FAILED";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_CODE = "extra_connection_failed_error_code";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_MESSAGE = "extra_connection_failed_error_message";

    public static final String ACTION_DRONE_EVENT = Utils.PACKAGE_NAME + ".ACTION_DRONE_EVENT";
    public static final String EXTRA_DRONE_EVENT = "extra_drone_event";

    private LocalBroadcastManager lbm;


    long millis4;

    TextView textoroll,textopitch,textoyaw, textomodoDrone;
    TextView textopanel;

    Runnable task,task2;
    long millis,millis5;
    long millisconexion;

    Handler handler2;
    Handler handlera;
    Handler handlerconexion;

    double lat;
    double lng;

    String proveedor;
    LocationManager locaManager;


    double latitude;
    double longitude;

    double latitudG;
    double longitudG;



    float latitud;
    float longitud;
    Handler handler5;

    File wFileE;
    String wDatos="";


    TextView textogpsLatitud;
    TextView textogpsLongitud;
    TextView gpsdrone;
    TextView textoConexion,tx1;
    TextView textoArmado;



    int alturaDeseada=1;
    boolean ARMADO=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_apm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        wFileE = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), EXTERNAL_FILENAME);
        ((SeekBar)findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(this);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                msg("");
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

       // setUpMapIfNeeded();

        final Context context = getApplicationContext();

        dpPrefs = new DroidPlannerPrefs(context);
        controlTower = new ControlTower(context);
        lbm = LocalBroadcastManager.getInstance(context);

        drone = new com.o3dr.android.client.Drone(context);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TOGGLE_DRONE_CONNECTION);

        registerReceiver(broadcastReceiver, intentFilter);

       // ToggleButton button=(ToggleButton) findViewById(R.id.toggleButton1);
       // button.setOnClickListener(this);

        handler5 =new Handler();
        handler2=new Handler();
        handlera =new Handler();

        handlerconexion =new Handler();
        millisconexion= SystemClock.uptimeMillis();
        new Thread(taskconexion).start();//to work in Background

        millis4= SystemClock.uptimeMillis();
     //  new Thread(task3).start();//to work in Background
        textogpsLatitud=(TextView)findViewById(R.id.textogps1);
        textogpsLongitud=(TextView)findViewById(R.id.textogps2);
        gpsdrone=(TextView)findViewById(R.id.gpsdrone);
        textoConexion= (TextView)findViewById(R.id.textoConexion);
        //textoArmado= (TextView)findViewById(R.id.textoArmado);
        tx1= (TextView)findViewById(R.id.tx1);



        setUpMapIfNeeded();
        modsigueme();
        verBoton();

        locaManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Location localizacion=locaManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        proveedor = LocationManager.NETWORK_PROVIDER;


        LocationListener locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location localizacion) {

                // int alt = Integer.parseInt(altura.getText().toString());

                latitudG = localizacion.getLatitude();
                longitudG = localizacion.getLongitude();
                // float longitud1=localizacion.getSpeed();
                //longlatid=latitud+longitud;
                //textoGPS.setText("angulo:"+app3.protocol.angx + "\n" +"altura:"+ alt + "\n" + "latitudd:" + latitud + "\n" + "longitudd:" + longitud + "\n" + "Latitud:" + (int) (latitud * 1e7) + "\n" + "Longitud:" + (int) (longitud * 1e7));
                // alt = Integer.parseInt(altura.getText().toString());
                textogpsLatitud.setText("Latitud: " + latitudG);
                textogpsLongitud.setText("Longitud: " + longitudG);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locaManager.requestLocationUpdates(proveedor, 0, 0, locListener);

    }


    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case ACTION_TOGGLE_DRONE_CONNECTION:
                    boolean connect = intent.getBooleanExtra(EXTRA_ESTABLISH_CONNECTION,
                            !drone.isConnected());

                    if (connect)
                        connectToDrone();
                    else
                        disconnectFromDrone();
                    break;
            }
        }
    };

                @Override
                protected void onDestroy() {
                    super.onDestroy();


                }

private final Runnable disconnectionTask = new Runnable() {
        @Override
        public void run() {
            // Timber.d("Starting control tower disconnect process...");
            controlTower.unregisterDrone(drone);
            controlTower.disconnect();
            handler.removeCallbacks(this);
        }
    };

  public void getTakeOffInAutoConfirmation(View view) {
        getDrone().post(new Runnable() {
            @Override
            public void run() {

                while (true){
                drone.setGuidedAltitude(3);
                drone.sendGuidedPoint(new LatLong(latitude, longitude), false);
                Log.w("xx", String.valueOf(new LatLong(latitude, longitude)));

                msg("coordenadas:" + latitude + " " + longitude);
            }
            }
        });

    }

////-------------------------------------------------------------- Almacenar datos

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void mensajeArmado(){

        if (drone.isConnected()){if(ARMADO)
        {
            mediaplayer = MediaPlayer.create(contexto, R.raw.conectado);
            mediaplayer.start();
        }}
    }


    public void guardaddatos(View v){

        if(isExternalStorageWritable()) {
//                    File wFileE = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), EXTERNAL_FILENAME);

            try {
                FileWriter out = new FileWriter(wFileE);
                out.append(String.valueOf(wDatos));
                out.close();
                Toast t = Toast.makeText(this, "Los datos fueron Guardados",
                        Toast.LENGTH_SHORT);
                t.show();


            } catch (IOException e) {
                Toast.makeText(this, "Error escribiendo fichero en memoria externa", Toast.LENGTH_SHORT).show();
            }
        }

    }
////--------------------------------------------------------------------------Fin guardar datos


    @Override
    public void onStart() {
        super.onStart();
        this.controlTower.connect(this);

    }
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_apm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.conectar:
                conectar();

             //   msg("conectando");
                return true;

            case R.id.desconectar:
                desconectar();
                dpPrefs.setBluetoothDeviceAddress("");

              //  msg("cerrar conexion");
                return true;

            case R.id.action_settings:
                iniciarsigume();

                //  msg("cerrar conexion");
                return true;
        }
        return super.onContextItemSelected(item);

    }


    public Drone getDrone() {
        return this.drone;
    }

    public void conectar() {
        final Drone drone = getDrone();
        connectToDrone();
          }

    public void desconectar() {
        final Drone drone = getDrone();
        if (drone != null && drone.isConnected()) disconnectFromDrone();
        if (!drone.isConnected()) {
            //  mediaplayer = MediaPlayer.create(this, R.raw.desconectado);
            // mediaplayer.start();}
        }
    }

    public static void disconnectFromDrone(Context context) {
        context.sendBroadcast(new Intent(MainApm.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(MainApm.EXTRA_ESTABLISH_CONNECTION, false));
    }

    public void disconnectFromDrone() {
        if (drone.isConnected()) {
            //   Timber.d("Disconnecting from drone.");
            drone.disconnect();
        }
    }


//////////////////////////////////////////////////////////////////////////////////Navegacion Draweri



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        boolean FragmetTransaccion=false;
        Fragment fragment=null;


        if (id == R.id.nav_camera) {
            fragment =new FragmentMapa();
            FragmetTransaccion=true;

            if (FragmetTransaccion){
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_apm,fragment)
                        .commit();
            }

           // setUpMapIfNeeded();

            Log.w("aaa","entro a la opcion camara");
         // Handle the camera action
        } else if (id == R.id.Panel) {

            fragment = new FragmentPanel();
            FragmetTransaccion=true;


            if (FragmetTransaccion){
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_apm,fragment)
                        .commit();


            }

        }else if (id == R.id.Postura) {

            fragment =new FragmentPostura();
            FragmetTransaccion=true;


            if (FragmetTransaccion){
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_apm,fragment)
                        .commit();


            }

        } else if (id == R.id.stabilize) {

            modoStabilize();


        } else if (id == R.id.loiter) {

            modoLoiter();

        }else if (id == R.id.guided) {

            modoGuided();

        }



        item.setChecked(true);
        getSupportActionBar().setTitle(item.getTitle());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

 //////////////////////////////////////End Navegacion Drawer

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {

    }

     TextView altitudTextView;

      protected void AltitudDrone() {

       altitudTextView = (TextView)findViewById(R.id.textoAltura_Drone);

          final Attitude postxyz = this.drone.getAttribute(AttributeType.ATTITUDE);
          float pitch =  (float)postxyz.getPitch();
          float roll =  (float)postxyz.getRoll();
          float yaw =  (float)postxyz.getYaw();

          AttitudeIndicator postura=(AttitudeIndicator)findViewById(R.id.postura1);
          postura.setAttitude((float) roll, (float) pitch, (float) yaw);

       final Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
       altitudTextView.setText(String.format("%3.1f", droneAltitude.getAltitude()) + "m");
    }


//////////////////////////////////..............Eventos Cuadricoptero

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        postura();
        verModoCuadricoptero();
        verdatos();
        AltitudDrone();
    }


///////////////////////////...............Fin Eventos Cuadricoptero

    public void  verModoCuadricoptero()
    {
        textomodoDrone=(TextView)findViewById(R.id.textomodoDrone);

        final ConnectionParameter connParams = retrieveConnectionParameters();
        if (connParams == null)
            return;
        final Drone drone = getDrone();

        final boolean isDroneConnected = drone.isConnected();
        final State droneState = drone.getAttribute(AttributeType.STATE);
        if (isDroneConnected) {
            textomodoDrone.setText(droneState.getVehicleMode().getLabel());

        }
    }


    public void modoStabilize(){
        final Drone drone = getDrone();

        final boolean isDroneConnected = drone.isConnected();
        final State dronemode= drone.getAttribute(AttributeType.STATE);
        if (isDroneConnected) {
            drone.changeVehicleMode(VehicleMode.COPTER_STABILIZE);
        }
    }

    public void modoLoiter(){
        final Drone drone = getDrone();

        final boolean isDroneConnected = drone.isConnected();
        final State dronemode= drone.getAttribute(AttributeType.STATE);
        if (isDroneConnected) {
            drone.changeVehicleMode(VehicleMode.COPTER_LOITER);
        }
    }

    public void modoGuided(){
        final com.o3dr.android.client.Drone drone = getDrone();

        final boolean isDroneConnected = drone.isConnected();
        final State dronemode= drone.getAttribute(AttributeType.STATE);
        if (isDroneConnected) {
            drone.changeVehicleMode(VehicleMode.COPTER_GUIDED);
            drone.setGuidedAltitude(alturaDeseada);
           // drone.sendGuidedPoint(coordenadas, false);

        }
    }

    public void botonGuided(View view){
                   drone.setGuidedAltitude(alturaDeseada);
                   drone.sendGuidedPoint(new LatLong(latitude, longitude), false);
                   Log.w("xx", String.valueOf(new LatLong(latitude, longitude)));

                   msg("coordenadas:" + latitude + " " + longitude);
                   // ControlApi.getApi(drone).goTo(latitu, longitu, false, null);

    }
///////////////////////////////////////////////////////////////////////////////Mapa de google

    private void setUpMap() {

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setMyLocationEnabled(true);

        final Marker[] perth = new Marker[1];

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                // TODO Auto-generated method stub
                latitude= arg0.latitude;
                longitude=arg0.longitude;
                perth[0] = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).draggable(true).title("Marker"));
                msg("arg0"+ arg0.latitude + "  " + arg0.longitude);
            }
        });

        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Marker"));


//        Gps droneGps = drone.getAttribute(AttributeType.GPS);
//
//        LatLong posicion=droneGps.getPosition();
//        float latitud= (float) posicion.getLatitude();
//        float longitud= (float) posicion.getLongitude();


            //  mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

    }

    private void setUpMapIfNeeded() {
       // if (drone.isConnected()) {
            // Do a null check to confirm that we have not already instantiated the map.
            if (mMap == null) {
                // Try to obtain the map from the SupportMapFragment.
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                        .getMap();
                // Check if we were successful in obtaining the map.
                if (mMap != null) {
                    setUpMap();
                }
            }
       // }
    }

///////////////////////////////////////////////////////////////////////Fin mapa de google


    public void modsigueme() {


        task2 = new Runnable() {
            @Override
            public void run() {

             runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drone.setGuidedAltitude(alturaDeseada);
                        drone.sendGuidedPoint(new LatLong(latitudG, longitudG), false);
                        Log.w("xx", String.valueOf(new LatLong(latitudG, longitudG)));
                       // textogps.setText("X:" + latitudG);

                    }
                });

                millis5 = millis5 + 1000;
                handler2.postAtTime(task2, millis5);


            }
        };
    }

    public void iniciarsigume (){

        millis5= SystemClock.uptimeMillis();
        handler2.post(task2);
    }

    public void detenerSigueme(View view){
    handler2.removeCallbacks(task2);
    }


                public MediaPlayer mediaplayer;
                Context contexto = this;
    Runnable taskconexion = new Runnable() {



     @Override
       public void run() {



         runOnUiThread(new Runnable() {
             @Override
             public void run() {

                 if (!drone.isConnected())
                 {
                 tx1.setText("Desarmado");
                 }
                if (drone.isConnected()){if(ARMADO)
                {
                    tx1.setText("Armado");
                  //  mensajeArmado();

                }}
                 if (drone.isConnected())
                 {if (!ARMADO)
                 {
                     tx1.setText("Desarmado");
                 }}

                 if (drone.isConnected())
                 {
                     textoConexion.setText("Conectado");
                 }
                 if (!drone.isConnected())
                 {
                     textoConexion.setText("Desconectado");
                 }
                 Log.w("xx", String.valueOf(new LatLong(latitudG, longitudG)));
                 // textogps.setText("X:" + latitudG);

             }
         });


         millisconexion = millisconexion + 1000;
        handlerconexion.postAtTime(taskconexion, millisconexion);
    }
    };

    public void posicioncuadricoptero()
    {
//        Gps droneGps = drone.getAttribute(AttributeType.GPS);
//
//        LatLong posicion=droneGps.getPosition();
//        float latitud= (float) posicion.getLatitude();
//        float longitud= (float) posicion.getLongitude();

        //mMap.addMarker(new MarkerOptions().position(new LatLng(posicion.getLatitude(), posicion.getLongitude())).title("Marker"));
    }
long tiempoPasado=System.nanoTime();
float sumaTiempo=0;
    public void postura() {

        long tiempoMedido=System.nanoTime();
        float tiempo=(float)(tiempoMedido-tiempoPasado)/1000000000;
        tiempoPasado=tiempoMedido;
        Log.w("tt", String.valueOf(sumaTiempo));
        sumaTiempo+=tiempo;

        textoroll=(TextView)findViewById(R.id.textoroll);
        textopitch=(TextView)findViewById(R.id.textopitch);
        textoyaw=(TextView)findViewById(R.id.textoyaw);
       // textopanel=(TextView)findViewById(R.id.textopanel);
       // textodeprueva=(TextView)findViewById(R.id.textomodoDrone);

        AttitudeIndicator postura=(AttitudeIndicator)findViewById(R.id.postura);

        final Attitude postxyz = this.drone.getAttribute(AttributeType.ATTITUDE);
        float pitch =  (float)postxyz.getPitch();
        float roll =  (float)postxyz.getRoll();
        float yaw =  (float)postxyz.getYaw();

        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        double altitudQuadracopter=droneAltitude.getAltitude();


        final State droneState = drone.getAttribute(AttributeType.STATE);

        if (droneState.isArmed())
        {
            ARMADO=true;
        }else{ARMADO=false;}

        postura.setAttitude((float) roll, (float) pitch, (float) yaw);
        textoroll.setText("Roll: " + String.valueOf(roll));
        textopitch.setText("Pitch: " + String.valueOf(pitch));
        textoyaw.setText("Yaw: "+String.valueOf(yaw));
       // textodeprueva.setText(droneState.getVehicleMode().getLabel());
       //textopanel.setText(droneState.getVehicleMode().getLabel());
        gpsdrone.setText(String.valueOf(latitud)+ "\n "+String.valueOf(longitud));


        Gps droneGps = drone.getAttribute(AttributeType.GPS);



        LatLong posicion=droneGps.getPosition();
        double latitud=  posicion.getLatitude();
        double longitud=  posicion.getLongitude();




        wDatos+= sumaTiempo+ "  "+pitch +"  "+ roll +"   "+ yaw + "  " + latitud + "  " + longitud + "  " + altitudQuadracopter + "  " + latitudG +"  " +longitudG+ "\n";
        Log.w("www", wDatos);

    }


    @Override
    public void onClick(View v) {

        final State droneState = getDrone().getAttribute(AttributeType.STATE);
        if(droneState.isArmed())
        {
            getDrone().arm(false);

        }
        else{
            getDrone().arm(true);
        }

        Log.w("sss", String.valueOf(droneState.isArmed()));


    }


    public void vistaBoton(){

        //State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        Button armButton = (Button) findViewById(R.id.botonarmar);

        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
                // armButton.setVisibility(View.INVISIBLE);
                if (!drone.isConnected()) {
                    armButton.setVisibility(View.INVISIBLE);
                } else {
                    armButton.setVisibility(View.VISIBLE);
                }
        if (vehicleState.isArmed()){
            armButton.setVisibility(View.INVISIBLE);
        }


    }

    public void verBoton (){
        // acelView.setText(String.valueOf(acelerometro.leerX()+"\n" + acelerometro.leerY()+"\n"+acelerometro.leerZ()));
        millis= SystemClock.uptimeMillis();
        handlera.post(task);

    }


    public void verdatos() {


        task = new Runnable() {
            @Override
            public void run() {


                vistaBoton();


                millis = millis + 1000;
                handlera.postAtTime(task, millis);


            }
        };
    }



    public void botonArmar(View view) {


        getDrone().arm(true);

        msg("Armar...");
    }


    public void botonDesarmar(View view) {
        //aState vehicleState = this.drone.getAttribute(AttributeType.STATE);
        // Button armButton = (Button) findViewById(R.id.botondearmado);

        getDrone().arm(false);
    }


    public void botonaltura(View view) {


        // final double takeOffAltitude = getAppPrefs().getDefaultAltitude();


        final State droneState = getDrone().getAttribute(AttributeType.STATE);
        final VehicleMode modoDrone = droneState.getVehicleMode();




        if (modoDrone==VehicleMode.COPTER_LOITER) {
            getDrone().doGuidedTakeoff(alturaDeseada);
             msg(String.valueOf(alturaDeseada));

        }
        Log.w("aa", String.valueOf(alturaDeseada));
    }

    public void botonaterrizar(View view)
    {

        msg("aterrizar");
        getDrone().changeVehicleMode(VehicleMode.COPTER_LAND);

    }



    public void botonRegresoCasa(View view){

        getDrone().changeVehicleMode(VehicleMode.COPTER_RTL);
        msg("Regreso a casa");
//        eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(VehicleMode.COPTER_RTL
//                .getLabel());

    }

///////////////////////////////-----------------------------------------Modo sigueme


    public void rutinaseguimiento(View view)
    {

        toggleFollowMe();
    }

    private void toggleFollowMe() {
        final Drone drone = getDrone();
        if (drone == null)
            return;

        final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
       //if (followState.isEnabled()) {
           // drone.disableFollowMe();
       // } else {
            //enableFollowMe(drone);

           // new Runnable() {
             //       @Override
               //     public void run() {
                        msg("Modo Sigueme");
                        drone.enableFollowMe(FollowType.LEASH);
                //    }
                //};

       // }

    }


//    private void enableFollowMe(final Drone drone) {
//        if(drone == null)
//            return;
//
//        final LocationRequest locationReq = LocationRequest.create()
//                .setPriority(FOLLOW_LOCATION_PRIORITY)
//                .setFastestInterval(FOLLOW_LOCATION_UPDATE_FASTEST_INTERVAL)
//                .setInterval(FOLLOW_LOCATION_UPDATE_INTERVAL)
//                .setSmallestDisplacement(FOLLOW_LOCATION_UPDATE_MIN_DISPLACEMENT);
//
//        final CheckLocationSettings locationSettingsChecker = new CheckLocationSettings(this, locationReq,
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        drone.enableFollowMe(FollowType.LEASH);
//                    }
//                });
//
//        locationSettingsChecker.check();
//    }
///////////////////////////////-------------------------------finalizar Modo Sigueme



    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }


    @Override
    public void onTowerDisconnected() {

    }

    @Override
    public void onTowerConnected() {
        //  Timber.d("Connecting to the control tower.");

        drone.unregisterDroneListener(this);
        controlTower.registerDrone(drone, handler);
        drone.registerDroneListener(this);

        //  notifyApiConnected();
    }

    public void connectToDrone() {



        final ConnectionParameter connParams = retrieveConnectionParameters();
        if (connParams == null)
            return;

        boolean isDroneConnected =drone.isConnected();

        Log.w("bbb", "estado de conexion " + isDroneConnected);
        if (!connParams.equals(drone.getConnectionParameter()) && isDroneConnected) {
            // Timber.d("Drone disconnection before reconnect attempt with different parameters.");
            drone.disconnect();
            isDroneConnected = false;
        }

        if (!isDroneConnected) {
            // Timber.d("Connecting to drone using parameter %s", connParams);
            drone.connect(connParams);
        }
    }

    public static void connectToDrone(Context context) {
        context.sendBroadcast(new Intent(MainApm.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(MainApm.EXTRA_ESTABLISH_CONNECTION, true));
    }




   String btAddress = null;

    private ConnectionParameter retrieveConnectionParameters() {
        final int connectionType = ConnectionType.TYPE_BLUETOOTH;
        Bundle extraParams = new Bundle();
        final DroneSharePrefs droneSharePrefs = new DroneSharePrefs(dpPrefs.getDroneshareLogin(), dpPrefs.getDronesharePassword(), dpPrefs.isDroneshareEnabled(), dpPrefs.isLiveUploadEnabled());

        ConnectionParameter connParams;
        switch (connectionType) {

            case ConnectionType.TYPE_BLUETOOTH:
                btAddress = dpPrefs.getBluetoothDeviceAddress();

                Log.w("cc", "conexion: " + (btAddress));



                if (TextUtils.isEmpty(btAddress))
                {
                    btAddress = null;
                    connParams = null;
                    startActivity(new Intent(getApplicationContext(), BluetoothDevicesActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));


                }
                else {

                    extraParams.putString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, btAddress);
                    connParams = new ConnectionParameter(connectionType, extraParams, droneSharePrefs);

                    Log.w("aaa", "Unrecognized connection type: " + connParams);
               }
                break;

            default:
                //Log.e(TAG, "Unrecognized connection type: " + connectionType);
              //  msg("entra al caso conectar bluetooth");
                connParams = null;
                break;
        }

        return connParams;
    }

/////////////metodos implemetados de GoogleMaps
    @Override
    public void onGoogleApiConnectionError(com.google.android.gms.common.ConnectionResult result) {

    }

    @Override
    public void onUnavailableGooglePlayServices(int status) {

    }

    @Override
    public void onManagerStarted() {

    }

    @Override
    public void onManagerStopped() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap= googleMap;

        Log.w("mmm", String.valueOf(lat +"   "+ lng));
    }


//////////////////////////////////////// endGoogleMaps

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    alturaDeseada=progress +1;
                    ((TextView)findViewById(R.id.progresoAltura)).setText("Altura:"+ alturaDeseada);

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }


}
