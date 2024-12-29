import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.jdatepicker.impl.*;
import org.jdatepicker.DateModel;

public class DateTimeSelector extends JPanel {
    private final JDatePickerImpl datePicker;
    private final JSpinner timeSpinner;
    private final java.util.List<LocalTime> availableTimes;
    
    public DateTimeSelector(String availableDays, String runtime) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        // Create date picker
        UtilDateModel model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        
        // Create time spinner
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        
        // Set current time as default
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        timeSpinner.setValue(calendar.getTime());
        
        // Filter available days
//        datePicker.addActionListener(e -> {
//            if (datePicker.getModel().getValue() != null) {
//                Date selectedDate = (Date) datePicker.getModel().getValue();
//                LocalDate selected = selectedDate.toInstant()
//                    .atZone(ZoneId.systemDefault())
//                    .toLocalDate();
//                String dayOfWeek = selected.getDayOfWeek().toString();
//                if (!isDateAvailable(dayOfWeek, availableDays)) {
//                    JOptionPane.showMessageDialog(this, 
//                        "Shows are not available on " + dayOfWeek);
//                    datePicker.getModel().setValue(null);
//                }
//            }
//        });
        
        // Create time selector based on runtime
        availableTimes = generateAvailableTimes(parseRuntime(runtime));
        
        // Add components with labels
        add(new JLabel("Date:"));
        add(datePicker);
        add(Box.createHorizontalStrut(20));
        add(new JLabel("Time:"));
        add(timeSpinner);
    }
    
    private java.util.List<LocalTime> generateAvailableTimes(int runtimeMinutes) {
        java.util.List<LocalTime> times = new ArrayList<>();
        LocalTime startTime = LocalTime.of(10, 0); // Theater opens at 10 AM
        LocalTime endTime = LocalTime.of(22, 0);   // Last show must end by 10 PM
        
        // Add buffer time between shows
        int bufferTime = 30; // 30 minutes buffer
        int totalSlotTime = runtimeMinutes + bufferTime;
        
        while (startTime.plusMinutes(runtimeMinutes).isBefore(endTime)) {
            times.add(startTime);
            startTime = startTime.plusMinutes(totalSlotTime);
            // Safety check to prevent infinite loops
            if (times.size() > 24) break; // Maximum 24 shows per day
        }
        
        return times;
    }
    
    private boolean isDateAvailable(String currentDay, String availableDays) {
        // Convert DayOfWeek enum value (e.g., "TUESDAY") to proper case (e.g., "Tuesday")
        String formattedCurrentDay = currentDay.substring(0, 1) + currentDay.substring(1).toLowerCase();

        // Split available days and normalize them
        String[] availableDayArray = availableDays.split(",");
        for (String day : availableDayArray) {
            // Trim whitespace and compare directly
            if (day.trim().equalsIgnoreCase(formattedCurrentDay)) {
                return true;
            }
        }
        return false;
    }


    public void reset() {
        // Reset date picker
        datePicker.getModel().setValue(null);

        // Reset time spinner to default (10:00 AM)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        timeSpinner.setValue(calendar.getTime());
    }

    
    private int parseRuntime(String runtime) {
        int minutes = 0;
        String[] parts = runtime.split(" ");
        for (String part : parts) {
            if (part.endsWith("h")) {
                minutes += Integer.parseInt(part.replace("h", "")) * 60;
            } else if (part.endsWith("min")) {
                minutes += Integer.parseInt(part.replace("min", ""));
            }
        }
        return minutes;
    }
    
    public LocalDateTime getSelectedDateTime() {
        if (datePicker.getModel().getValue() == null) return null;
        
        Date selectedDate = (Date) datePicker.getModel().getValue();
        LocalDate date = selectedDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
            
        Date timeValue = (Date) timeSpinner.getValue();
        LocalTime time = timeValue.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalTime();
            
        return LocalDateTime.of(date, time);
    }
    
    public void updateSettings(String availableDays, String runtime) {
        // Reset time spinner
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        timeSpinner.setValue(calendar.getTime());
        
        // Update available times
        availableTimes.clear();
        availableTimes.addAll(generateAvailableTimes(parseRuntime(runtime)));
        
        // Reset date picker if current selection is invalid
        if (datePicker.getModel().getValue() != null) {
            Date selectedDate = (Date) datePicker.getModel().getValue();
            LocalDate selected = selectedDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            String dayOfWeek = selected.getDayOfWeek().toString();
            if (!isDateAvailable(dayOfWeek, availableDays)) {
                datePicker.getModel().setValue(null);
            }
        }
        
        revalidate();
        repaint();
    }
    
    private static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) {
            if (value != null) {
                if (value instanceof Calendar) {
                    return dateFormatter.format(((Calendar) value).getTime());
                } else if (value instanceof Date) {
                    return dateFormatter.format((Date) value);
                }
            }
            return "";
        }
    }

}
