/**
 * 12-12-2007
 * @author Piotr
 */
package pl.magot.vetch.ancal;

import java.util.*;

import pl.magot.vetch.ancal.agenda.*;
import pl.magot.vetch.ancal.database.*;
import pl.magot.vetch.ancal.dataview.*;
import pl.magot.vetch.ancal.views.*;
import pl.magot.vetch.ancal.reminder.AlarmService;
import pl.magot.vetch.widgets.*;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.*;
import android.widget.LinearLayout.*;
import android.content.Intent;
import android.os.*;
import android.content.*;

//Main activity
public class AnCal extends CommonActivity {
    private Calendar dateToday = Calendar.getInstance();
    private AgendaViewType currentAgendaViewType = AgendaViewType.TODAY;

    // menu items
    private final int miNewAppt = Menu.FIRST;
    private final int miNewTask = Menu.FIRST + 1;
    private final int miNewNote = Menu.FIRST + 2;
    private final int miShowAllTasks = Menu.FIRST + 3;
    private final int miOptions = Menu.FIRST + 4;
    private final int miAbout = Menu.FIRST + 5;

    // views
    private ScrollView scrollViewAgenda = null;
    protected RelativeLayout rlayAgendaTop = null;

    @SuppressWarnings("all")
    protected RelativeLayout rlayAgenda = null;

    @SuppressWarnings("all")
    protected RelativeLayout rlayAgendaView = null;
    protected LinearLayout llayAgendaData = null;

    // views
    private AgendaView CurrentAgendaView = null;
    private AgendaViewToday AgendaViewToday = null;
    private AgendaViewDay AgendaViewDay = null;
    private AgendaViewWeek AgendaViewWeek = null;
    private AgendaViewMonth AgendaViewMonth = null;

    // fields
    protected DataViewAppointment dataViewAppt = null;
    protected DataViewTask dataViewTask = null;
    protected DataViewNote dataViewNote = null;

    // fields
    private Handler handlerUpdateDate = new Handler();
    private Handler handlerUpdateView = new Handler();
    private final static int iHandlerUpdateTime = 1000 * 5;
    private int iUpdateDate_minute = 0;

    // views
    protected TextView labWeekStr = null;
    protected TextView labWeekNr = null;

    private TextView labSelectViewItem = null;
    private ViewImgButton btnSelectViewItemPrev = null;
    private Button btnSelectViewItemToday = null;
    private ViewImgButton btnSelectViewItemNext = null;

    // bottom buttons
    private Button btnSetViewToday = null;
    private Button btnSetViewDay = null;
    private Button btnSetViewWeek = null;
    private Button btnSetViewMonth = null;

    // methods
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.agenda);
        InitViews();
        InitStateOnce();
        StartReminderService();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handlerUpdateDate.removeCallbacks(handlerUpdateDateTask);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // build menu
        MenuItem mi = menu.add(0, miNewAppt, 0, R.string.actionNewAppointment);
        mi.setShortcut('1', 'a');
        mi.setIcon(R.drawable.menuiconappt);

        mi = menu.add(0, miNewTask, 1, R.string.actionNewTask);
        mi.setShortcut('2', 't');
        mi.setIcon(R.drawable.menuicontask);

        mi = menu.add(0, miNewNote, 2, R.string.actionNewNote);
        mi.setShortcut('3', 'n');
        mi.setIcon(R.drawable.menuiconnote);

        mi = menu.add(2, miShowAllTasks, 3, R.string.actionShowAllTasks);
        mi.setCheckable(true);
        mi.setChecked(prefs.bShowAllTasks);
        menuItemUpdateIcons(mi);

        mi = menu.add(4, miOptions, 4, R.string.actionOptions);
        mi.setShortcut('5', 'o');
        mi.setIcon(R.drawable.menuiconprefs);

        mi = menu.add(4, miAbout, 5, R.string.actionAbout);
        mi.setIcon(R.drawable.menuiconabout);

        return true;
    }

    public void menuItemUpdateIcons(MenuItem item) {
        if (item.getItemId() == miShowAllTasks)
            if (item.isChecked()) {
                item.setIcon(R.drawable.menuiconshowtasksdue);
            } else {
                item.setIcon(R.drawable.menuiconshowtasks);
            }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case miNewAppt:
            openActAppointment(-1, -1, -1);
            break;
        case miNewTask:
            openActTask(-1);
            break;
        case miNewNote:
            openActNote(-1);
            break;
        case miShowAllTasks: {
            item.setChecked(!item.isChecked());
            prefs.bShowAllTasks = item.isChecked();
            prefs.Save();
            RefreshData();
            menuItemUpdateIcons(item);
            break;
        }
        case miOptions:
            openActOptions();
            break;
        case miAbout:
            openActViewAbout();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    // initialize views
    private void InitViews() {
        // localize DateWidget
        DateWidget.setStrings(utils.GetResStr(R.string.strDateWidgetSelect), utils
                .GetResStr(R.string.strDateWidgetSelected), utils.GetResStr(R.string.strDateWidgetNone));

        // localize TimeWidget
        TimeWidget.setStrings(utils.GetResStr(R.string.strTimeWidgetSelect), utils
                .GetResStr(R.string.strTimeWidgetNone), utils.GetResStr(R.string.strTimeWidgetSet));

        rlayAgendaTop = (RelativeLayout) findViewById(R.id.rlayAgendaTop);
        rlayAgenda = (RelativeLayout) findViewById(R.id.rlayAgenda);
        rlayAgendaView = (RelativeLayout) findViewById(R.id.rlayAgendaView);

        llayAgendaData = (LinearLayout) findViewById(R.id.llayAgendaData);

        btnSelectViewItemPrev = (ViewImgButton) findViewById(R.id.btnSelectViewItemPrev);
        btnSelectViewItemPrev.SetButtonIcon(R.drawable.btnprev, -1);
        btnSelectViewItemPrev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CurrentAgendaView.SetPrevViewItem();
                RefreshAgendaAfterViewItemChange();
            }
        });

        btnSelectViewItemToday = (Button) findViewById(R.id.btnSelectViewItemToday);
        btnSelectViewItemToday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CurrentAgendaView.SetTodayViewItem();
                RefreshAgendaAfterViewItemChange();
            }
        });

        btnSelectViewItemNext = (ViewImgButton) findViewById(R.id.btnSelectViewItemNext);
        btnSelectViewItemNext.SetButtonIcon(R.drawable.btnnext, 0);
        btnSelectViewItemNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CurrentAgendaView.SetNextViewItem();
                RefreshAgendaAfterViewItemChange();
            }
        });

        labWeekStr = (TextView) findViewById(R.id.labWeekStr);
        labWeekNr = (TextView) findViewById(R.id.labWeekNr);

        labSelectViewItem = (TextView) findViewById(R.id.labSelectViewItem);

        // initialize change view bottom buttons
        btnSetViewToday = (Button) findViewById(R.id.btnSetViewToday);
        btnSetViewToday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // ToggleBottomButtonsState(btnSetViewToday);
                SetAgendaView(AgendaViewType.TODAY, LatestDateToday());
            }
        });

        btnSetViewDay = (Button) findViewById(R.id.btnSetViewDay);
        btnSetViewDay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // ToggleBottomButtonsState(btnSetViewDay);
                SetAgendaView(AgendaViewType.DAY, LatestDateToday());
            }
        });

        btnSetViewWeek = (Button) findViewById(R.id.btnSetViewWeek);
        btnSetViewWeek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // ToggleBottomButtonsState(btnSetViewWeek);
                SetAgendaView(AgendaViewType.WEEK, LatestDateToday());
            }
        });

        btnSetViewMonth = (Button) findViewById(R.id.btnSetViewMonth);
        btnSetViewMonth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // ToggleBottomButtonsState(btnSetViewMonth);
                SetAgendaView(AgendaViewType.MONTH, LatestDateToday());
            }
        });

        // initialize data views
        dataViewAppt = new DataViewAppointment(userdb, prefs);
        dataViewTask = new DataViewTask(userdb, prefs);
        dataViewNote = new DataViewNote(userdb, prefs);

        InitAgendaViewToday();
    }

    public void ToggleBottomButtonsState(CompoundButton btnClicked) {
        btnClicked.setEnabled(false);
        btnClicked.requestFocus();

        if (btnClicked != btnSetViewToday)
            btnSetViewToday.setEnabled(true);
        if (btnClicked != btnSetViewDay)
            btnSetViewDay.setEnabled(true);
        if (btnClicked != btnSetViewWeek)
            btnSetViewWeek.setEnabled(true);
        if (btnClicked != btnSetViewMonth)
            btnSetViewMonth.setEnabled(true);
    }

    public void UpdateBottomButtonsStateByCurrentView() {
        /*
         * if (iCurrentAgendaViewType == AgendaView.viewMode.TODAY)
         * ToggleBottomButtonsState(btnSetViewToday); if (iCurrentAgendaViewType
         * == AgendaView.viewMode.DAY) ToggleBottomButtonsState(btnSetViewDay);
         * if (iCurrentAgendaViewType == AgendaView.viewMode.WEEK)
         * ToggleBottomButtonsState(btnSetViewWeek); if (iCurrentAgendaViewType
         * == AgendaView.viewMode.MONTH)
         * ToggleBottomButtonsState(btnSetViewMonth);
         */
    }

    public void InitAgendaViewToday() {
        // initialize today agenda view
        AgendaViewToday = new AgendaViewToday(this);
        AgendaViewToday.Rebuild();

        // set click event for all agenda views
        AgendaView.SetItemClick(new AgendaView.OnViewItemClick() {
            public void OnClick(View v, Bundle extras) {
                long lRowId = extras.getLong(CommonActivity.bundleRowId);
                if (extras.getString("type").equals(ViewTodayItemHeader.ViewType.Appointments.toString())) {
                    if (extras.containsKey(CommonActivity.bundleHourOfDay)) {
                        openActAppointment(lRowId, extras.getInt(CommonActivity.bundleHourOfDay), extras
                                .getInt(CommonActivity.bundleMinutes));
                    } else {
                        openActAppointment(lRowId, -1, -1);
                    }
                }
                if (extras.getString("type").equals(ViewTodayItemHeader.ViewType.Tasks.toString()))
                    openActTask(lRowId);
                if (extras.getString("type").equals(ViewTodayItemHeader.ViewType.Notes.toString()))
                    openActNote(lRowId);
            }
        });

        // initialize all other agenda views
        AgendaViewDay = new AgendaViewDay(AnCal.this);
        AgendaViewDay.Rebuild();

        AgendaViewWeek = new AgendaViewWeek(AnCal.this);
        AgendaViewWeek.Rebuild();

        AgendaViewMonth = new AgendaViewMonth(AnCal.this);
        AgendaViewMonth.Rebuild();

        // initialize scrollable content
        scrollViewAgenda = new ScrollView(this);
        LayoutParams layParams = new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
                android.view.ViewGroup.LayoutParams.FILL_PARENT);
        scrollViewAgenda.setLayoutParams(layParams);
    }

    private void InitStateOnce() {
        ReloadAllDataTables();
        SetAgendaView(AgendaViewType.TODAY, LatestDateToday());

        // refresh view (relayout bug)
        ForceUpdateLayout();

        // schedule handler update date task
        handlerUpdateDate.removeCallbacks(handlerUpdateDateTask);
        handlerUpdateDate.postDelayed(handlerUpdateDateTask, iHandlerUpdateTime);

        // focus default button
        btnSetViewToday.requestFocus();
    }

    private void ForceUpdateLayout() {
        handlerUpdateView.removeCallbacks(handlerUpdateViewTask);
        handlerUpdateView.postDelayed(handlerUpdateViewTask, 100);
    }

    private Runnable handlerUpdateViewTask = new Runnable() {
        public void run() {
            rlayAgenda.postInvalidate();
            rlayAgenda.requestLayout();
            rlayAgendaView.postInvalidate();
            rlayAgendaView.requestLayout();
        }
    };

    private Runnable handlerUpdateDateTask = new Runnable() {
        public void run() {
            try {
                UpdateTodayDate();

                // refresh data, if system timer minute changed
                if (iUpdateDate_minute != dateToday.get(Calendar.MINUTE)) {
                    iUpdateDate_minute = dateToday.get(Calendar.MINUTE);

                    // autorefresh only today view
                    if (currentAgendaViewType == AgendaViewType.TODAY) {
                        if (CurrentAgendaView != null)
                            CurrentAgendaView.SetViewStartDate(LatestDateToday());
                        RefreshData();
                    }
                }
            } finally {
                handlerUpdateDate.postDelayed(this, iHandlerUpdateTime);
            }
        }
    };

    // refresh start view date
    public synchronized void UpdateTodayDate() {
        dateToday.setTimeInMillis(System.currentTimeMillis());
        dateToday.setFirstDayOfWeek(prefs.iFirstDayOfWeek);
    }

    public synchronized void RefreshData() {
        // set title
        SetActivityTitle(AnCalDateUtils.formatLongDate(this, dateToday));

        // set week nr label
        SetWeekNrText(CurrentAgendaView.GetViewStartDate());

        // update weeknr view
        UpdateWeekNrInfoVisibility();

        // update view info view
        UpdateSelectViewText(currentAgendaViewType);

        // update button text date
        UpdateCurrentViewItemDate();

        // reload data
        if (userdb.DatabaseReady()) {
            // filter data
            dataViewAppt.FilterData(CurrentAgendaView.GetViewStartDate(), CurrentAgendaView.GetViewType());
            if (currentAgendaViewType == AgendaViewType.TODAY) {
                dataViewTask.FilterData(CurrentAgendaView.GetViewStartDate(), CurrentAgendaView.GetViewType());
                dataViewNote.FilterData(CurrentAgendaView.GetViewStartDate(), CurrentAgendaView.GetViewType());
            }
            // rebuild views
            CurrentAgendaView.RebuildViewAppointments(dataViewAppt);
            if (currentAgendaViewType == AgendaViewType.TODAY) {
                CurrentAgendaView.RebuildViewTasks(dataViewTask);
                CurrentAgendaView.RebuildViewNotes(dataViewNote);
            }
        }

        // set scroll view top position
        scrollViewAgenda.scrollTo(0, 0);
    }

    public void RefreshAgendaAfterViewItemChange() {
        btnSelectViewItemToday.setText("...");
        SetWeekNrText(CurrentAgendaView.GetViewStartDate());
        UpdateWeekNrInfoVisibility();

        // filter data
        dataViewAppt.FilterData(CurrentAgendaView.GetViewStartDate(), CurrentAgendaView.GetViewType());

        // rebuild view
        CurrentAgendaView.RebuildViewAppointments(dataViewAppt);

        UpdateCurrentViewItemDate();
    }

    public void UpdateCurrentViewItemDate() {
        String s = "";
        switch (currentAgendaViewType) {
        case DAY:
        case WEEK:
            s = AnCalDateUtils.formatMediumDate(this, CurrentAgendaView.GetViewStartDate());
            break;
        case MONTH:
            s = AnCalDateUtils.formatMediumDate(this, CurrentAgendaView.GetCurrentSelectedMonthAsCalendar());
            break;
        default:
            s = "";
        }

        btnSelectViewItemToday.setText(s);
    }

    // main program date holder
    public Calendar getDateToday() {
        dateToday.setFirstDayOfWeek(prefs.iFirstDayOfWeek);
        return dateToday;
    }

    public Calendar LatestDateToday() {
        UpdateTodayDate();
        return getDateToday();
    }

    public void SetWeekNrText(Calendar date) {
        int iNr = date.get(Calendar.WEEK_OF_YEAR);
        labWeekNr.setText(Integer.toString(iNr));
    }

    public void UpdateWeekNrInfoVisibility() {
        if (currentAgendaViewType == AgendaViewType.MONTH) {
            labWeekStr.setVisibility(View.INVISIBLE);
            labWeekNr.setVisibility(View.INVISIBLE);
        } else {
            labWeekStr.setVisibility(View.VISIBLE);
            labWeekNr.setVisibility(View.VISIBLE);
        }
    }

    public void ReloadDataTable(String sTableNameToReload) {
        if (userdb.DatabaseReady()) {
            if (sTableNameToReload.equals(Database.sTableNameAppointments))
                dataViewAppt.ReloadTable();
            if (sTableNameToReload.equals(Database.sTableNameTasks))
                dataViewTask.ReloadTable();
            if (sTableNameToReload.equals(Database.sTableNameNotes))
                dataViewNote.ReloadTable();
        }
    }

    public void ReloadAllDataTables() {
        if (userdb.DatabaseReady()) {
            dataViewAppt.ReloadTable();
            dataViewTask.ReloadTable();
            dataViewNote.ReloadTable();
        }
    }

    private void UpdateSelectViewText(AgendaViewType viewType) {
        String s = "";
        if (currentAgendaViewType == AgendaViewType.DAY)
            s = utils.GetResStr(R.string.labSelectViewDay);
        if (currentAgendaViewType == AgendaViewType.WEEK)
            s = utils.GetResStr(R.string.labSelectViewWeek);
        if (currentAgendaViewType == AgendaViewType.MONTH)
            s = utils.GetResStr(R.string.labSelectViewMonth);
        labSelectViewItem.setText(s);
    }

    public synchronized void SetAgendaView(AgendaViewType viewType, Calendar calViewDate) {
        if (userdb.DatabaseReady()) {
            // init view
            currentAgendaViewType = viewType;

            ShowTopControls(currentAgendaViewType != AgendaViewType.TODAY);

            // change type
            if (currentAgendaViewType == AgendaViewType.TODAY)
                CurrentAgendaView = AgendaViewToday;
            if (currentAgendaViewType == AgendaViewType.DAY)
                CurrentAgendaView = AgendaViewDay;
            if (currentAgendaViewType == AgendaViewType.WEEK)
                CurrentAgendaView = AgendaViewWeek;
            if (currentAgendaViewType == AgendaViewType.MONTH)
                CurrentAgendaView = AgendaViewMonth;

            if (CurrentAgendaView != null)
                CurrentAgendaView.SetViewStartDate(calViewDate);

            // reload data
            RefreshData();

            // set view change buttons
            UpdateBottomButtonsStateByCurrentView();

            VisibleLayoutContentRemove();
            VisibleLayoutContentAdd();

        } else {
            utils.ShowMsgResStr(Database.GetErrDesc(userdb.TablesCreationResult()), MessageType.ERROR);
        }
    }

    private void VisibleLayoutContentRemove() {
        llayAgendaData.removeAllViews();
        scrollViewAgenda.removeAllViews();
    }

    private void VisibleLayoutContentAdd() {
        if (currentAgendaViewType == AgendaViewType.TODAY) {
            scrollViewAgenda.addView(CurrentAgendaView.GetParentLayout());
            llayAgendaData.addView(scrollViewAgenda);
        }
        if (currentAgendaViewType == AgendaViewType.DAY) {
            scrollViewAgenda.addView(CurrentAgendaView.GetParentLayout());
            llayAgendaData.addView(scrollViewAgenda);
        }
        if (currentAgendaViewType == AgendaViewType.WEEK) {
            llayAgendaData.addView(CurrentAgendaView.GetParentLayout());
        }
        if (currentAgendaViewType == AgendaViewType.MONTH) {
            llayAgendaData.addView(CurrentAgendaView.GetParentLayout());
        }
    }

    private void ShowTopControls(boolean bEnable) {
        // enable
        btnSelectViewItemPrev.setEnabled(bEnable);
        btnSelectViewItemToday.setEnabled(bEnable);
        btnSelectViewItemNext.setEnabled(bEnable);
        rlayAgendaTop.setEnabled(bEnable);

        // focus
        btnSelectViewItemPrev.setFocusable(bEnable);
        btnSelectViewItemToday.setFocusable(bEnable);
        btnSelectViewItemNext.setFocusable(bEnable);
        rlayAgendaTop.setFocusable(bEnable);

        // visibility
        btnSelectViewItemPrev.setVisibility((bEnable) ? View.VISIBLE : View.INVISIBLE);
        btnSelectViewItemToday.setVisibility((bEnable) ? View.VISIBLE : View.INVISIBLE);
        btnSelectViewItemNext.setVisibility((bEnable) ? View.VISIBLE : View.INVISIBLE);

        // resize layout
        MarginLayoutParams mlp = (MarginLayoutParams) rlayAgendaTop.getLayoutParams();
        mlp.setMargins(0, 0, 0, 0);
        rlayAgendaTop.getLayoutParams().height = 0;
        if (bEnable) {
            rlayAgendaTop.getLayoutParams().height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            mlp.setMargins(0, 0, 0, 6);
        }

        // refresh view (relayout bug)
        ForceUpdateLayout();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // todo...
    }

    public void openActAppointment(long lRowId, int iHourOfDay, int iMinutes) {
        bundleOtherDataStartup.clear();
        if (lRowId == -1) {
            bundleOtherDataStartup.putInt(CommonActivity.bundleAgendaView, CurrentAgendaView.GetViewIndex());
            bundleOtherDataStartup.putLong(CommonActivity.bundleAgendaViewStartDate, CurrentAgendaView
                    .GetViewStartDate().getTimeInMillis());

            if (iHourOfDay != -1)
                bundleOtherDataStartup.putInt(CommonActivity.bundleHourOfDay, iHourOfDay);
            if (iMinutes != -1)
                bundleOtherDataStartup.putInt(CommonActivity.bundleMinutes, iMinutes);

            OpenActivity(0, "android.intent.action.AnCal.ACTION_MODE_NEW_APPOINTMENT");
        } else {
            OpenActivity(0, "android.intent.action.AnCal.ACTION_MODE_EDIT_APPOINTMENT", lRowId);
        }
    }

    public void openActTask(long lRowId) {
        bundleOtherDataStartup.clear();
        if (lRowId == -1)
            OpenActivity(0, "android.intent.action.AnCal.ACTION_MODE_NEW_TASK");
        else
            OpenActivity(0, "android.intent.action.AnCal.ACTION_MODE_EDIT_TASK", lRowId);
    }

    public void openActNote(long lRowId) {
        bundleOtherDataStartup.clear();
        if (lRowId == -1)
            OpenActivity(0, "android.intent.action.AnCal.ACTION_MODE_NEW_NOTE");
        else
            OpenActivity(0, "android.intent.action.AnCal.ACTION_MODE_EDIT_NOTE", lRowId);
    }

    public void openActOptions() {
        bundleOtherDataStartup.clear();
        OpenActivity(0, "android.intent.action.AnCal.ACTION_MODE_EDIT_OPTIONS");
    }

    public void openActViewAbout() {
        bundleOtherDataStartup.clear();
        OpenActivity(0, "android.intent.action.AnCal.ACTION_MODE_VIEW_ABOUT");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle extras = CommonActivity.getIntentExtras(data);
        if (extras != null) {
            if (resultCode == RESULT_OK) {
                // state refresh, because of table updated
                if (extras.containsKey(CommonActivity.bundleTableUpdated)) {
                    String sTableNameToReload = extras.getString(CommonActivity.bundleTableUpdated);
                    ReloadDataTable(sTableNameToReload);
                    RefreshData();
                    UpdateReminderService(this, prefs, CommonActivity.bundleTableUpdated);
                }
                // state refresh, because of options updated
                if (extras.containsKey(CommonActivity.bundleOptionsUpdated)) {
                    prefs.Load();
                    if (CurrentAgendaView.TimeFormatChanged())
                        CurrentAgendaView.UpdateTimeFormat();
                    CurrentAgendaView.SetTodayViewItem();
                    RefreshData();
                    UpdateReminderService(this, prefs, CommonActivity.bundleOptionsUpdated);
                }
            }
        }
    }

    public int GetViewSpaceWidth() {
        // return rlayAgendaView.getWidth();
        return 320 - 8;
    }

    public int GetViewSpaceHeight() {
        // return rlayAgendaView.getHeight();
        return 340;
    }

    public boolean StartReminderService() {
        ComponentName cpn = startService(new Intent(AnCal.this, AlarmService.class));
        return (cpn != null);
    }

    /*
     * public void StopReminderService() { stopService(new Intent(AnCal.this,
     * AlarmService.class)); }
     */

    public static boolean UpdateReminderService(Context context, Prefs prefs, String sKey) {
        Bundle args = new Bundle();
        args.putBoolean(sKey, true);
        // put additional prefs
        args.putBoolean("b24HourMode", prefs.b24HourMode);
        args.putInt("iFirstDayOfWeek", prefs.iFirstDayOfWeek);
        args.putInt("iSnoozeCount", prefs.iSnoozeCount);
        args.putInt("iSnoozeMinutesOverdue", prefs.iSnoozeMinutesOverdue);
        // update service
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtras(args);
        ComponentName cpn = context.startService(intent);
        return (cpn != null);
    }

    @Override
    protected void restoreStateFromFreeze() {

    }

}
