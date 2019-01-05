package ffournier.sample.graphpolygon;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.Random;

import ffournier.libscoregraphview.ScoreFactor;

/**
 * Main Activity to choose the scor Graph
 */
public class MainActivity extends AppCompatActivity {

    private LinearLayout mLLScoreColor;
    private LinearLayout mLLScoreTitle;
    private LinearLayout mLLScoreScore;
    private EditText mEdtNbScore;
    private Button mBtnOK;

    private int mNbScore = 1;
    public static final String KEY_LIST_FACTOR = "ffournier.sample.graphpolygon.list_factor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLLScoreScore = findViewById(R.id.ll_score);
        mLLScoreColor = findViewById(R.id.ll_color);
        mLLScoreTitle = findViewById(R.id.ll_title);
        mEdtNbScore = findViewById(R.id.edt_nb_score_graph);
        mBtnOK = findViewById(R.id.btn_valid);
        mEdtNbScore.setText(String.valueOf(mNbScore));

        initView();
    }

    private void initView() {

        mEdtNbScore.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0) {
                    String text = editable.toString();
                    try {
                        int value = Integer.parseInt(text);
                        if (value >= 0) {
                            if (value != mNbScore) {
                                mNbScore = value;
                                refreshLayouts();
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        });

        mBtnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScoreGraphActivity.class);
                intent.putParcelableArrayListExtra(KEY_LIST_FACTOR, getFactors());
                startActivity(intent);
            }
        });

        refreshLayouts();
    }

    /**
     * Get Factors
     * @return the factors given by user
     */
    private ArrayList<ScoreFactor> getFactors() {
        ArrayList<ScoreFactor> list = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < mNbScore; ++i) {
            float score = random.nextInt(100) / 100.0f;
            String title = "Default " + random.nextInt(1000);
            int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
            EditText edtScore = (EditText) mLLScoreScore.getChildAt(i);
            if(edtScore != null) {
                try {
                    score = Float.parseFloat(edtScore.getText().toString());
                } catch (NumberFormatException e) {

                }
            }

            View viewColor = mLLScoreColor.getChildAt(i);
            if (viewColor != null)  {
                color = ((ColorDrawable) viewColor.getBackground()).getColor();
            }

            EditText edtTitle = (EditText) mLLScoreTitle.getChildAt(i);
            if(edtTitle != null) {
                String titleFound = edtTitle.getText().toString();
                if (!titleFound.isEmpty())
                    title =  titleFound;
            }
            list.add(new ScoreFactor(score, title, color));
        }
        return list;
    }

    /**
     * Display Input for factors
     */
    private void refreshLayouts() {
        ArrayList<ScoreFactor> oldFactors = getFactors();

        mLLScoreColor.removeAllViewsInLayout();
        mLLScoreTitle.removeAllViewsInLayout();
        mLLScoreScore.removeAllViewsInLayout();

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,64,getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5,getResources().getDisplayMetrics());
        int heightColor = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,54,getResources().getDisplayMetrics());

        Random random = new Random();

        for (int i = 0; i < mNbScore; ++i) {
            EditText edtScore = new EditText(MainActivity.this);
            edtScore.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

            edtScore.setText(i < oldFactors.size() ? String.valueOf(oldFactors.get(i).mScore) : String.valueOf(random.nextInt(100) / 100.0f));
            edtScore.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL);
            mLLScoreScore.addView(edtScore);

            EditText edtTitle = new EditText(MainActivity.this);
            edtTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            edtTitle.setText(i < oldFactors.size() ? oldFactors.get(i).mTitle : "Default " + random.nextInt(1000));
            mLLScoreTitle.addView(edtTitle);

            View viewColor = new View(MainActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightColor);
            params.setMargins(margin, margin, margin, margin);
            viewColor.setLayoutParams(params);
            int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

            viewColor.setBackground(new ColorDrawable(i < oldFactors.size() ? oldFactors.get(i).mColor :
                    color));
            viewColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   showColorPicker(view);
                }
            });
            mLLScoreColor.addView(viewColor);
        }
    }

    /**
     * Show Color Picker
     * @param view : the view associate
     */
    private void showColorPicker(final View view) {
        ColorPickerDialogBuilder
                .with(MainActivity.this)
                .setTitle("Choose color")
                .initialColor(((ColorDrawable) view.getBackground()).getColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        view.setBackground(new ColorDrawable(selectedColor));
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }
}
