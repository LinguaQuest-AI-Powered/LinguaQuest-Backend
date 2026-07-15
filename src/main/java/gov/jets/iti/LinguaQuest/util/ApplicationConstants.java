package gov.jets.iti.LinguaQuest.util;

public class ApplicationConstants {

    public static String JWT_SECRET_KEY = "JWT_SECRET_KEY";
    public static String JWT_DEFAULT_SECRET_KEY = "b4ef8faacf4371e54b99899216b6aeefd91b4286ad4d5e7eafb5a60834872acb";
    public static String JWT_EXPIRATION = "JWT_EXPIRATION";
    public static String JWT_HEADER = "Authorization";
    public ApplicationConstants() {
        throw new AssertionError("Can't instaniate ApplicationConstants class");
    }
}
