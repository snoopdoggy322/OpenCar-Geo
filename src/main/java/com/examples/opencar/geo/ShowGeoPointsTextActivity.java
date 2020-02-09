
package com.examples.opencar.geo;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class ShowGeoPointsTextActivity extends Activity
{
  private ListView geoPointsList;
  private Button showAsMapButton, filterButton, previousPageButton, nextPageButton;
  private TextView categoryTitleTextView;

  private ArrayAdapter adapter;

  private List<GeoPoint> geoPointsCollection;
  private BackendlessGeoQuery geoQuery;
  private int totalPages;
  private int currentPageNumber;

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.geopoints_text );

    currentPageNumber = 0;

    List<String> chosenCategories = new ArrayList<String>();
    chosenCategories.add( MapShowActivity.category );
    geoQuery = new BackendlessGeoQuery();
    geoQuery.setCategories( chosenCategories );
    if( !MapShowActivity.whereClause.isEmpty() )
    {
      geoQuery.setWhereClause( MapShowActivity.whereClause );
    }
    if( MapShowActivity.searchInRadius )
    {
      if( MapShowActivity.mGoogleApiClient.isConnected() )
      {
        Location mCurrentLocation = MapShowActivity.getLastLocation(this);
        geoQuery.setLatitude( mCurrentLocation.getLatitude() );
        geoQuery.setLongitude( mCurrentLocation.getLongitude() );
        geoQuery.setRadius( MapShowActivity.radius );
        geoQuery.setUnits( MapShowActivity.units );
      }
      else
      {
        Toast.makeText( this, "Current location not available", Toast.LENGTH_LONG ).show();
      }
    }

    Backendless.Geo.getPoints( geoQuery, new DefaultCallback<List<GeoPoint>>( this )
    {
      @Override
      public void handleResponse( final List<GeoPoint> response )
      {
        Backendless.Geo.getGeopointCount(geoQuery, new DefaultCallback<Integer>( ShowGeoPointsTextActivity.this )
        {
          @Override
          public void handleResponse( final Integer totalPoints )
          {
            totalPages = (int) Math.ceil( (double) totalPoints / response.size() );
            initList( response );
            initButtons();
            super.handleResponse( totalPoints );
          }
        });
        super.handleResponse( response );
      }
    } );

    initUI();
  }

  private void initUI()
  {
    geoPointsList = (ListView) findViewById( R.id.geoPointsList );
    showAsMapButton = (Button) findViewById( R.id.showAsMapButton );
    filterButton = (Button) findViewById( R.id.filterButton );
    previousPageButton = (Button) findViewById( R.id.previousPageButton );
    nextPageButton = (Button) findViewById( R.id.nextPageButton );
    categoryTitleTextView = (TextView) findViewById( R.id.categoryTitle );

    String title = String.format( getResources().getString( R.string.browsing_category_page_title ), MapShowActivity.category );
    categoryTitleTextView.setText( title );

    showAsMapButton.setOnClickListener( new View.OnClickListener()
    {
      @Override
      public void onClick( View v )
      {
        startActivity( new Intent( ShowGeoPointsTextActivity.this, MapShowActivity.class ) );
        finish();
      }
    } );

    filterButton.setOnClickListener( new View.OnClickListener()
    {
      @Override
      public void onClick( View v )
      {
        startActivity( new Intent( ShowGeoPointsTextActivity.this, FilterActivity.class ) );
        finish();
      }
    } );

    previousPageButton.setOnClickListener( new View.OnClickListener()
    {
      @Override
      public void onClick( View view )
      {
        geoQuery.prepareForPreviousPage();
        Backendless.Geo.getPoints( geoQuery, new DefaultCallback<List<GeoPoint>>( ShowGeoPointsTextActivity.this )
        {
          @Override
          public void handleResponse( List<GeoPoint> response )
          {
            currentPageNumber--;
            initList( response );
            initButtons();

            super.handleResponse( response );
          }
        } );
      }
    } );

    nextPageButton.setOnClickListener( new View.OnClickListener()
    {
      @Override
      public void onClick( View view )
      {
        geoQuery.prepareForNextPage();
        Backendless.Geo.getPoints( geoQuery, new DefaultCallback<List<GeoPoint>>( ShowGeoPointsTextActivity.this )
        {
          @Override
          public void handleResponse( List<GeoPoint> response )
          {
            currentPageNumber++;
            initList( response );
            initButtons();

           super.handleResponse( response );
          }
        } );
      }
    } );
  }

  private void initList( List<GeoPoint> response )
  {
    geoPointsCollection = response;

    String[] geoPointsStringArray = new String[ geoPointsCollection.size() ];
    for( int i = 0; i < geoPointsCollection.size(); i++ )
    {
      GeoPoint geoPoint = geoPointsCollection.get( i );
      geoPointsStringArray[ i ] = String.format( getResources().getString( R.string.geo_points_text_list_item_template ), i + 1, geoPoint.getLatitude().toString(), geoPoint.getLongitude().toString() );
    }

    adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, geoPointsStringArray );
    geoPointsList.setAdapter( adapter );
  }

  private void initButtons()
  {
    previousPageButton.setEnabled( currentPageNumber > 0 );
    nextPageButton.setEnabled( currentPageNumber < totalPages );
  }
}
                        