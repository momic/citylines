package org.citylines.view.dialog.date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.DatePicker;
import android.widget.EditText;
import java.util.Calendar;
import static org.citylines.model.Constant.INPUT_DATETIME_FORMAT;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author zlaja
 */
public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    
    public final static String DATE_EDIT_ID = "dateEditId";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, yy, mm, dd);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Fragment parent = getTargetFragment();
        
        String dateEditId = getArguments().getString(DATE_EDIT_ID);
        int resId = getResources().getIdentifier(dateEditId, "id", parent.getActivity().getPackageName());
        EditText dateEdit = (EditText) parent.getView().findViewById(resId);

        LocalDate current = new LocalDate(year, monthOfYear+1, dayOfMonth);
        dateEdit.setText(DateTimeFormat.forPattern(INPUT_DATETIME_FORMAT).print(current));
    }
}