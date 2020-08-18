
package com.examples.opencar.geo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.geo.GeoCategory;

import java.util.List;

public class GeoCategoriesListActivity extends Activity
{
  public static int REQUEST_ACCESS_COARSE_LOCATION = 1001;
  private ListView geoCategoriesListView;
  private List<GeoCategory> geoCategories;

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.geo_categories_list );

    initUI();

    if( ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
    {
      ActivityCompat.requestPermissions( this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION );
    }

    Backendless.setUrl( Defaults.SERVER_URL );
    Backendless.initApp( this, Defaults.APPLICATION_ID, Defaults.API_KEY );

    Backendless.Geo.getCategories( new DefaultCallback<List<GeoCategory>>( this )
    {
      @Override
      public void handleResponse( List<GeoCategory> response )
      {
        geoCategories = response;

        String[] geoCategoriesStringArray = new String[ geoCategories.size() ];
        for( int i = 0; i < geoCategories.size(); i++ )
        {
          geoCategoriesStringArray[ i ] = geoCategories.get( i ).getName();
        }

        ArrayAdapter adapter = new ArrayAdapter<String>( GeoCategoriesListActivity.this, R.layout.list_item_with_arrow, R.id.itemName, geoCategoriesStringArray );
        geoCategoriesListView.setAdapter( adapter );

        super.handleResponse( response );
      }
    } );
  }

  private void initUI()
  {
    geoCategoriesListView = (ListView) findViewById( R.id.geoCategoriesList );
    Intent mapIntent = new Intent( GeoCategoriesListActivity.this, MapShowActivity.class );
    mapIntent.putExtra( "category","readyCars" );
    startActivity( mapIntent );
    finish();
    geoCategoriesListView.setOnItemClickListener( new AdapterView.OnItemClickListener()
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id )
      {
        TextView textView = (TextView) view.findViewById( R.id.itemName );
        String text = textView.getText().toString();

        Intent mapIntent = new Intent( GeoCategoriesListActivity.this, MapShowActivity.class );
        mapIntent.putExtra( "category","geoservice_sample" );
        startActivity( mapIntent );
        finish();
      }
    } );
  }

  @Override
  public void onRequestPermissionsResult( int requestCode,
                                          @NonNull
                                                  String[] permissions,
                                          @NonNull
                                                  int[] grantResults )
  {
    super.onRequestPermissionsResult( requestCode, permissions, grantResults );
    Toast.makeText( this, "Permissions granted successfully", Toast.LENGTH_SHORT );
  }

}
                        