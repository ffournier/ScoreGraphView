package ffournier.sample.graphpolygon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import ffournier.libscoregraphview.ScoreFactor;
import ffournier.libscoregraphview.ScoreGraphView;

public class ScoreGraphActivity extends AppCompatActivity {

    ScoreGraphView scoreGraphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoregraph);



        scoreGraphView = findViewById(R.id.score_graph_view);

        if (getIntent() == null || !getIntent().hasExtra(MainActivity.KEY_LIST_FACTOR))
            throw new RuntimeException();

        List<ScoreFactor> factors = getIntent().getParcelableArrayListExtra(MainActivity.KEY_LIST_FACTOR);
        scoreGraphView.setScoreFactors(factors);
    }
}
