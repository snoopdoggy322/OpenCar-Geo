
package com.examples.opencar.geo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.backendless.geo.Units;

public class FilterActivity extends Activity
{
  private TextView categoryTitle, queryText;
  private Spinner geoUnitsSpinner;
  private Button editQueryButton, applyButton, clearButton;

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.filter );

    initUI();
  }

  private void initUI()
  {
    categoryTitle = (TextView) findViewById( R.id.categoryTitle );
    queryText = (TextView) findViewById( R.id.queryText );
    editQueryButton = (Button) findViewById( R.id.queryEditButton );
    applyButton = (Button) findViewById( R.id.applyButton );
    clearButton = (Button) findViewById( R.id.clearButton );
    geoUnitsSpinner = (Spinner) findViewById( R.id.geoUnitsSpinner );

    String title = String.format( getResources().getString( R.string.browsing_category_page_title ), MapShowActivity.category );
    categoryTitle.setText( title );

    queryText.setText( MapShowActivity.whereClause );

    editQueryButton.setOnClickListener( new View.OnClickListener()
    {
      @Override
      public void onClick( View v )
      {
        onEditQueryButtonClicked();
      }
    } );

    applyButton.setOnClickListener( new View.OnClickListener()
    {
      @Override
      public void onClick( View v )
      {
        onApplyButtonClicked();
      }
    } );

    clearButton.setOnClickListener( new View.OnClickListener()
    {
      @Override
      public void onClick( View v )
      {
        onClearButtonClicked();
      }
    } );

    ArrayAdapter<CharSequence> geoUnitsSpinnerAdapter = ArrayAdapter.createFromResource( this, R.array.geo_units, android.R.layout.simple_spinner_item );
    geoUnitsSpinnerAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
    geoUnitsSpinner.setAdapter( geoUnitsSpinnerAdapter );
  }

  private void onClearButtonClicked()
  {
    MapShowActivity.whereClause = "";
    MapShowActivity.searchInRadius = false;
    startActivity( new Intent( this, MapShowActivity.class ) );
    finish();
  }

  private void onApplyButtonClicked()
  {
    MapShowActivity.whereClause = queryText.getText().toString();
    RadioButton radiusRadio = (RadioButton) findViewById( R.id.radiusRadio );
    EditText radiusEdit = (EditText) findViewById( R.id.radiusEdit );
    if( radiusRadio.isChecked() )
    {
      MapShowActivity.searchInRadius = true;
      MapShowActivity.radius = Double.parseDouble( radiusEdit.getText().toString() );
      MapShowActivity.units = Units.valueOf( geoUnitsSpinner.getSelectedItem().toString() );
    }
    else
    {
      MapShowActivity.searchInRadius = false;
    }
    startActivity( new Intent( this, MapShowActivity.class ) );
    finish();
  }

  private void onEditQueryButtonClicked()
  {
    startActivity( new Intent( FilterActivity.this, QueryEditActivity.class ) );
    finish();
  }
}
                        