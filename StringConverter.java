package socialnetwork.utils;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class StringConverter extends javafx.util.StringConverter
{

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String toString(Object object) {
        if(object==null)return "";
        return dateTimeFormatter.format((TemporalAccessor) object);
    }

    @Override
    public java.time.LocalDate fromString(String string)
    {
        if(string==null||string.trim().isEmpty()) return null;
        return java.time.LocalDate.parse(string,dateTimeFormatter);
    }
}
