package com.examples.opencar.geo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersActivity extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ExpandableListView expandableListView;

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


        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList();

            }
        });
        mSwipeRefreshLayout.setRefreshing(true);
        updateList();
    }


    private void updateList() {
        String whereClause = "ownerId = '" + Backendless.UserService.CurrentUser().getObjectId() + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        Backendless.Data.of("RentDetails").find(queryBuilder, new AsyncCallback<List<Map>>() {
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

                    Date date = (Date) group.get("created");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM HH:mm", myDateFormatSymbols);
                    dateFormat.format(date);
                    String title=dateFormat.format( date );
                    title+=String.format("%12.2f",(group.get("value")))+" \u20BD";
                    map.put("groupName", title); // время года
                    groupDataList.add(map);
                    Map mc = new HashMap<>();

                    String content="Время в пути - "+String.valueOf((int)group.get("time_in_way"))+"\n";
                    mc.put("monthName", content);
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

                expandableListView = (ExpandableListView) findViewById(R.id.expListView);
                expandableListView.setAdapter(adapter);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("DEBUGG", fault.toString());

            }
        });

    }

    private static DateFormatSymbols myDateFormatSymbols = new DateFormatSymbols() {

        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
    };
}
