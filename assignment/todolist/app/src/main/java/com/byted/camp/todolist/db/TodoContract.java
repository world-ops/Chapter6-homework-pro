package com.byted.camp.todolist.db;

import android.icu.text.UnicodeSetSpanner;
import android.provider.BaseColumns;

import com.byted.camp.todolist.operation.db.FeedReaderContract;

import java.util.Date;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public class TodoContract {

    // TODO 定义表结构和 SQL 语句常量
    public static final String SQL_CREATE_LISTS=
            "CREATE TABLE " + TodoList.TABLE_NAME + " (" +
                    TodoList._ID + " INTEGER PRIMARY KEY," +
                    TodoList.COLUMN_NAME_THING + " TEXT," +
                    TodoList.COLUMN_NAME_TIME + " TEXT," +
                    TodoList.COLUMN_NAME_STATE + " TEXT," +
                    TodoList.COLUMN_NAME_PRIORITY + " TEXT)";

    public static final String SQL_DELETE_TodoList =
            "DROP TABLE IF EXISTS " + TodoList.TABLE_NAME;
    private TodoContract() {
    }

    public static class TodoList implements BaseColumns{
        public static final String TABLE_NAME = "todo_list";
        public static final String COLUMN_NAME_THING = "thing";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_STATE = "state";
        public static final String COLUMN_NAME_PRIORITY = "priority";
    }


}
