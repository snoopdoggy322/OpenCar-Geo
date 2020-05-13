package com.examples.opencar.geo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_back);
        myToolbar.setTitle("Мои поездки");
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();

            }
        });
        String whereClause = "client_id = '"+Backendless.UserService.CurrentUser().getObjectId()+"'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause( whereClause );
        Backendless.Data.of("orders").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                Map<String, String> map;
                ArrayList<Map<String, String>> groupDataList = new ArrayList<>();
                // создаем общую коллекцию для коллекций элементов
                ArrayList<ArrayList<Map<String, String>>> сhildDataList = new ArrayList<>();
                // в итоге получится сhildDataList = ArrayList<сhildDataItemList>
                // создаем коллекцию элементов для первой группы
                ArrayList<Map<String, String>> сhildDataItemList = new ArrayList<>();

                for (Map group : response) {
                    // заполняем список атрибутов для каждой группы
                    map = new HashMap<>();
                    map.put("groupName", group.get("time_start_reserve").toString()); // время года
                    groupDataList.add(map);
                    Map mc = new HashMap<>();
                    mc.put("monthName", group.toString());
                    сhildDataItemList = new ArrayList<>();
                    сhildDataItemList.add(mc);
                    сhildDataList.add(сhildDataItemList);
                }
                // список атрибутов групп для чтения
                String groupFrom[] = new String[]{"groupName"};
                // список ID view-элементов, в которые будет помещены атрибуты групп
                int groupTo[] = new int[]{android.R.id.text1};

                // список атрибутов элементов для чтения
                String childFrom[] = new String[]{"monthName"};
                // список ID view-элементов, в которые будет помещены атрибуты
                // элементов
                int childTo[] = new int[]{android.R.id.text1};

                SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                        OrdersActivity.this, groupDataList,
                        android.R.layout.simple_expandable_list_item_1, groupFrom,
                        groupTo, сhildDataList, android.R.layout.simple_list_item_1,
                        childFrom, childTo);

                ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expListView);
                expandableListView.setAdapter(adapter);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("DEBUGG",fault.toString());

            }
        });



    }
    }

