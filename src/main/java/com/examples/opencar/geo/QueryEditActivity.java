
package com.examples.opencar.geo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class QueryEditActivity extends Activity
{
  private EditText queryEdit;
  private TextView categoryTitle;

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.edit_query );

    initUI();
  }

  private void initUI()
  {
    categoryTitle = (TextView) findViewById( R.id.categoryTitle );
    queryEdit = (EditText) findViewById( R.id.queryEdit );

    String title = String.format( getResources().getString( R.string.browsing_category_page_title ), MapShowActivity.category );
    categoryTitle.setText( title );

    queryEdit.setText( MapShowActivity.whereClause );
    queryEdit.setOnEditorActionListener( new TextView.OnEditorActionListener()
    {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event )
      {
        if( actionId == EditorInfo.IME_ACTION_DONE )
        {
          onDoneClicked();
        }
        return true;
      }
    } );
  }

  private void onDoneClicked()
  {
    MapShowActivity.whereClause = queryEdit.getText().toString();
    startActivity( new Intent( this, FilterActivity.class ) );
    finish();
  }
}
                        