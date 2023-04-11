package com.dmbteam.scheduler.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dobrikostadinov on 5/8/15.
 */
@DatabaseTable(tableName = "dbitem")
public class DbItem {
    public static final SimpleDateFormat DATE_FORMATTER_WITH_HOUR = new SimpleDateFormat("dd-MMMM-yyyy - HH:mm");

    @DatabaseField(columnName = "id", canBeNull = false, generatedId = true, unique = true)
    private int id;

    @DatabaseField(columnName = "time")
    private long time;

    @DatabaseField(columnName = "date")
    private long date;

    @DatabaseField(columnName = "numbers")
    private String numbers;

    @DatabaseField(columnName = "message")
    private String message;

    @DatabaseField(columnName = "is_sent")
    private boolean isSent;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setIsSent(boolean isDeleted) {
        this.isSent = isDeleted;
    }

    public String getName() {

        String resultName = "";

        if (numbers != null && numbers.length() > 0) {
            String[] numbersArray = numbers.split(";");

            if (numbersArray != null && numbersArray.length > 0) {
                resultName += numbersArray[0].split("\\^")[0];

                if (numbersArray.length > 1) {
                    resultName += "  +" + (numbersArray.length - 1);
                }
            }
        }

        return resultName;

    }

    public List<String> getPhoneNumbersAsList() {

        List<String> resultNumbers = new ArrayList<String>();

        if (numbers != null && numbers.length() > 0) {
            String[] numbersArray = numbers.split(";");

            if (numbersArray != null) {
                for (int i = 0; i < numbersArray.length; i++) {
                    resultNumbers.add(numbersArray[i].split("\\^")[1]);
                }
            }
        }

        return resultNumbers;
    }

    public boolean isValidNumbers(){

        if(getPhoneNumbersAsList().size() > 0 && !getPhoneNumbersAsList().get(0).equals("null")){
            return true;
        }

        return false;
    }



    public String getDateAndTimeAsString() {

        String dateString = DATE_FORMATTER_WITH_HOUR.format(new Date(getDate() + getTime()));

        return dateString;
    }

    public Date getDateAndTimeAsDate() {

        return new Date(getDate() + getTime());
    }


}
