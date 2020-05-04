package com.byted.camp.todolist;

import android.app.Activity;
import android.arch.persistence.room.Query;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;


    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
                notesAdapter.refresh(loadNotesFromDatabase());
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
                notesAdapter.refresh(loadNotesFromDatabase());
            }
        });
        recyclerView.setAdapter(notesAdapter);

        Log.i("LINYUEBEI", "onCreate: before");
        dbHelper = new TodoDbHelper(this);
        Log.i("LINYUEBEI", "onCreate: after");
        notesAdapter.refresh(loadNotesFromDatabase());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {

        // TODO 从数据库中查询数据，并转换成 JavaBeans
        List<Note> result = new ArrayList<Note>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        /*
        String[] projection = {
                TodoContract.TodoList._ID,
                TodoContract.TodoList.COLUMN_NAME_THING,
                TodoContract.TodoList.COLUMN_NAME_TIME,
                TodoContract.TodoList.COLUMN_NAME_STATE
        };
        */

        String sortOrder = TodoContract.TodoList.COLUMN_NAME_PRIORITY + " ASC";

        Cursor cursor = db.query(false, TodoContract.TodoList.TABLE_NAME, null, null,null,null,
                null,sortOrder,null);


        while (cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(TodoContract.TodoList._ID));
            Log.i("LINYUEBEI", "loadNotesFromDatabase: ");

            Note temp = new Note(itemId);
            String thing = cursor.getString(cursor.getColumnIndex(TodoContract.TodoList.COLUMN_NAME_THING));
            temp.setContent(thing);

            SimpleDateFormat df = new SimpleDateFormat("E,d MMM yyyy HH:mm:ss"/*, Locale.ENGLISH*/);
            String time1 = cursor.getString(cursor.getColumnIndex(TodoContract.TodoList.COLUMN_NAME_TIME));
            try {
                java.util.Date time = df.parse(time1);
                temp.setDate(time);}
            catch (ParseException e){
                e.printStackTrace();
                System.out.println("字符串转为date类型是发生异常");
            }

            String state1 = cursor.getString(cursor.getColumnIndex(TodoContract.TodoList.COLUMN_NAME_STATE));
            int state2 = Integer.parseInt(state1);
            State state = State.from(state2);
            temp.setState(state);


            Log.i("LINYUEBEI", "here" );
            String priority2 = cursor.getString(cursor.getColumnIndex(TodoContract.TodoList.COLUMN_NAME_PRIORITY));
            int priority = Integer.parseInt(priority2);
            temp.setPriority(priority);
            Log.i("LINYUEBEI", "here2" );

            result.add(temp);
        }
        Log.i("LINYUEBEI", "load all success" );
        cursor.close();
        return result;
        //return null;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define 'where' part of query.
        String selection = TodoContract.TodoList._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(note.id)};
        // Issue SQL statement.
        int deletedRows = db.delete(TodoContract.TodoList.TABLE_NAME, selection, selectionArgs);
        //Log.i(TAG, "perform delete data, result:" + deletedRows);

    }

    private void updateNode(Note note) {
        // 更新数据

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoList.COLUMN_NAME_STATE,note.getState().intValue);

        // Which row to update
        // Define 'where' part of query.
        String selection = TodoContract.TodoList._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(note.id)};

        int count = db.update(
                TodoContract.TodoList.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        Log.i("LINYUEBEI", "perform update data, result:" + count + note.getState());

    }

}

