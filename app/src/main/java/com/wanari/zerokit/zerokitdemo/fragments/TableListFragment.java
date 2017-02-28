package com.wanari.zerokit.zerokitdemo.fragments;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.wanari.zerokit.zerokitdemo.R;
import com.wanari.zerokit.zerokitdemo.adapters.TableRecyclerViewAdapter;
import com.wanari.zerokit.zerokitdemo.common.AppConf;
import com.wanari.zerokit.zerokitdemo.database.FireBaseHelper;
import com.wanari.zerokit.zerokitdemo.entities.Table;
import com.wanari.zerokit.zerokitdemo.interfaces.IMain;
import com.wanari.zerokit.zerokitdemo.interfaces.ITableList;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableListFragment extends Fragment implements ITableList {

    private RecyclerView mTableList;

    private Button mAddNewTableBtn;

    private TableRecyclerViewAdapter mTableRecyclerViewAdapter;

    private IMain parentListener;

    public static TableListFragment newInstance() {

        Bundle args = new Bundle();

        TableListFragment fragment = new TableListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tablelist, container, false);
        mTableList = (RecyclerView) view.findViewById(R.id.tableList);
        mAddNewTableBtn = (Button) view.findViewById(R.id.aaddNewTableBtn);
        setListeners();
        getData();
        return view;
    }

    private void setListeners() {
        mAddNewTableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                final EditText newTableEdit = (EditText) getActivity().getLayoutInflater().inflate(R.layout.dialog_new_table, null);
                alertBuilder.setView(newTableEdit);
                alertBuilder.setTitle(getString(R.string.new_table));
                alertBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (newTableEdit.getText() != null && newTableEdit.getText().length() > 0) {
                            FireBaseHelper.getInstance().saveTable(new Table(newTableEdit.getText().toString()));
                        }
                    }
                });
                alertBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertBuilder.create().show();
            }
        });
    }

    private void getData() {
        FireBaseHelper.getInstance().getTableLists(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Table> tableNames = new ArrayList<>();
                List<Table> alreadyAddedList = AppConf.getAddedTableNames();
                for (DataSnapshot tableSnapshot : dataSnapshot.getChildren()) {
                    Map<String, String> map = (HashMap<String, String>) tableSnapshot.getValue();
                    Table table = new Table(tableSnapshot.getKey(), map);
                    if (!alreadyAddedList.contains(table)) {
                        tableNames.add(table);
                    }
                }
                initLayout(tableNames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initLayout(List<Table> tableNames) {
        if (mTableRecyclerViewAdapter == null) {
            mTableRecyclerViewAdapter = new TableRecyclerViewAdapter(this, tableNames);
            mTableList.setAdapter(mTableRecyclerViewAdapter);
        } else {
            mTableRecyclerViewAdapter.setItems(tableNames);
        }
    }

    @Override
    public void tableItemSelected(Table table) {
        AppConf.putTable(table);
        mTableRecyclerViewAdapter.removeItem(table);
    }

    @Override
    public void closeTableList() {
        if (parentListener != null) {
            parentListener.closeTableList();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof IMain) {
            parentListener = (IMain) getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem = menu.findItem(R.id.search_table);
        if (menuItem != null) {
            menuItem.setVisible(false);
        }
    }
}
