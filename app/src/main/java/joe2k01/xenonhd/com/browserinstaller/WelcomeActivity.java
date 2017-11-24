package joe2k01.xenonhd.com.browserinstaller;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class WelcomeActivity extends Activity {

    private Button next;
    private ImageButton info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        next = findViewById(R.id.next);
        info = findViewById(R.id.info);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent main = new Intent(getApplicationContext(), joe2k01.xenonhd.com.browserinstaller.Activity.class);
                finish();
                startActivity(main);

            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent web = new Intent(getApplicationContext(), WebActivity.class);
                startActivity(web);

            }
        });
    }
}
