package org.citylines.view.dialog.date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.DatePicker;
import android.widget.EditText;
import java.util.Calendar;
import org.citylines.db.dao.CalendarDAO;
import org.citylines.db.dao.factory.DAOFactory;
import org.citylines.db.dao.factory.DAOType;
import org.citylines.model.calendar.DepartureDate;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 *
 * @author zlaja
 */
public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    
    public final static String DATE_EDIT_ID = "dateEditId";
    private EditText dateEdit;
    private CalendarDAO holidayDAO;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get dateEdit inside parent
        Fragment parent = getTargetFragment();
        
        String dateEditId = getArguments().getString(DATE_EDIT_ID);
        int resId = getResources().getIdentifier(dateEditId, "id", parent.getActivity().getPackageName());
        dateEdit = (EditText) parent.getView().findViewById(resId);        
        
        // DAO object
        holidayDAO = (CalendarDAO) DAOFactory.build(DAOType.HOLIDAY_DAO, getActivity());
        
        // Show dialog with current date
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        
        return new DatePickerDialog(getActivity(), this, yy, mm, dd);
    }
    
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        
        // get DateTime at StartOfDay for current date, including user's TimeZone
        final LocalDateTime currentDate = new LocalDate(year, monthOfYear+1, dayOfMonth).toLocalDateTime(LocalTime.MIDNIGHT);
        final DepartureDate current = new DepartureDate(currentDate, holidayDAO.isNationalHoliday(currentDate));
        
        dateEdit.setTag(current);
        dateEdit.setText(current.getDateInputString());
    }
}