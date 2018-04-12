package user;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String status;
    public String image;
    public String thumb_image;
    public String device_token;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String status, String image, String thumb_image, String device_token) {
        this.username = username;
        this.email = email;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
        this.device_token = device_token;
    }
}