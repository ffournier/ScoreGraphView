package ffournier.sample.graphpolygon;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import ffournier.libscoregraphview.ScoreFactor;
import ffournier.libscoregraphview.ScoreGraphView;

public class MainActivity extends AppCompatActivity {

    ScoreGraphView scoreGraphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreGraphView = findViewById(R.id.score_graph_view);

        List<ScoreFactor> factors = new ArrayList<>();
        factors.add(new ScoreFactor(0.5f, "Test", Color.MAGENTA));
        factors.add(new ScoreFactor(0.4f, "Test1", Color.YELLOW));
        factors.add(new ScoreFactor(0.3f, "Test2", Color.LTGRAY));
        factors.add(new ScoreFactor(0.2f, "Test3", Color.MAGENTA));
        factors.add(new ScoreFactor(0.1f, "Test4", Color.YELLOW));
        factors.add(new ScoreFactor(0.6f, "Test5", Color.MAGENTA));
        factors.add(new ScoreFactor(0.8f, "Test7", Color.LTGRAY));
        factors.add(new ScoreFactor(0.9f, "Test8", Color.MAGENTA));
        factors.add(new ScoreFactor(1.0f, "Test9", Color.YELLOW));
        scoreGraphView.setScoreFactors(factors);
    }
}
