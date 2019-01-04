package ffournier.libscoregraphview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.List;

/**
 * Class ScoreGraphView
 * Display the score of a survey with a graph
 */
public class ScoreGraphView extends View {

    // Variable Declaration
    private Paint mPaint;
    private Paint mPaintText;
    private Paint mPaintScore;


    // Data
    private List<ScoreFactor> mScoreFactors;
    private float percent = 0;
    private ValueAnimator mAnimator;

    private boolean isVisible = false;


    /**
     * Constructor
     * @param context : context
     */
    public ScoreGraphView(Context context) {
        super(context);
        initView(context, null, 0);
    }

    /**
     * Constructor
     * @param context :context
     * @param attrs : attributes
     */
    public ScoreGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    /**
     * Constructor
     * @param context : context
     * @param attrs : attributes
     * @param defStyleAttr : style attributes
     */
    public ScoreGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScoreGraphView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs, defStyleAttr);
    }

    /**
     * Init View
     * @param context : context
     * @param attrs : attributes
     * @param defStyleAttr : style attributes
     */
    private void initView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        int textSize = 20;
        int colorTitle = Color.BLACK;
        int colorLine = Color.BLACK;
        boolean animationEnabled = false;
        int animationDuration = 500;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ScoreGraphView, 0, 0);

            try {
                colorLine = a.getColor(R.styleable.ScoreGraphView_colorLine, Color.BLACK);
                colorTitle = a.getColor(R.styleable.ScoreGraphView_colorTitle, Color.BLACK);
                textSize = a.getDimensionPixelSize(R.styleable.ScoreGraphView_textSizeTitle, 20);
                animationEnabled = a.getBoolean(R.styleable.ScoreGraphView_animationEnabled, false);
                animationDuration = a.getInteger(R.styleable.ScoreGraphView_animationDuration, 500);

            } finally {
                a.recycle();
            }

        }

        // init Paint
        mPaint = new Paint();
        mPaint.setColor(colorLine);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);

        mPaintText = new Paint();
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setColor(colorTitle);
        mPaintText.setTextSize(textSize);

        mPaintScore = new Paint();
        mPaintScore.setAlpha(128);
        mPaintScore.setStyle(Paint.Style.FILL);


        if (animationEnabled) {
            mAnimator = ValueAnimator.ofFloat(0, 1.0f);
            mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mAnimator.setDuration(animationDuration);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    percent = (float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();

        int[] rect = new int[2];
        getLocationOnScreen(rect);
        if (rect[0] < width) {
            // out of screen
            if (!isVisible) {
                mAnimator.start();
            }
            isVisible = true;
            if (mScoreFactors != null) {
                switch (mScoreFactors.size()) {
                    case 0:
                        break;
                    case 1:
                        drawProgressBarGraph(canvas, mScoreFactors.get(0), width, height);
                        break;
                    case 2:
                        drawDiamondGraph(canvas,mScoreFactors ,width, height);
                        break;
                    default:
                        drawPolygon(canvas, mScoreFactors, width, height);
                        break;
                }
            }
        } else {
            // refresh all 50 ms
            isVisible = false;
            postInvalidateDelayed(50);
        }

    }

    private int changeDarknessIntToColor(int color, boolean dark) {
        int r =  (( color >> 16) & 0xff);
        int g =  (( color >> 8) & 0xff);
        int b =  (( color     ) & 0xff) ;
        int a =  (( color >> 24) & 0xff);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (dark) {
            hsv[2] = hsv[2] - 0.2f;
        } else {
            hsv[1] = hsv[1] - 0.2f;
        }
        return Color.HSVToColor(a, hsv);
    }

    private void drawProgressBarGraph(Canvas canvas, ScoreFactor scoreFactor, int width, int height){

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        float widthRect = width * 0.8f;
        float heightRect = height * 0.2f;
        float radius = 0.5f;
        float margin = mPaint.getStrokeWidth() * 3;

        float left = centerX - widthRect / 2.0f + margin;
        float factor = scoreFactor.mScore * percent ;
        float right = left +  (widthRect - margin) * factor;

        RectF rectScore = new RectF(left , centerY - heightRect / 2.0f + margin ,
                right,centerY + heightRect / 2.0f - margin );
        LinearGradient gradient = new LinearGradient(rectScore.left, rectScore.top, rectScore.right, rectScore.bottom,
                changeDarknessIntToColor(scoreFactor.mColor, false), scoreFactor.mColor , Shader.TileMode.CLAMP);
        mPaintScore.setDither(true);
        mPaintScore.setAntiAlias(true);
        mPaintScore.setShader(gradient);
        canvas.drawRoundRect(rectScore,heightRect * radius, heightRect * radius,  mPaintScore);

        RectF rect = new RectF(centerX - widthRect / 2.0f, centerY - heightRect / 2.0f,
                centerX + widthRect / 2.0f , centerY + heightRect / 2.0f);
        canvas.drawRoundRect(rect, heightRect * radius, heightRect * radius,  mPaint);

        mPaintText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(scoreFactor.mTitle,centerX,centerY - heightRect, mPaintText);

    }

    private void drawDiamondGraph(Canvas canvas,List<ScoreFactor> scoreFactors,  int width, int height){
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        float triangleHeight =  width / 2.0f * 0.8f;
        float triangleBase = height / 2.0f * 0.5f;
        float graduationHeight = height / 30.0f;

        float p0x =  centerX;
        float p0y =   centerY - triangleBase / 2.0f;
        float p1x ;
        float p1y = centerY;
        LinearGradient gradient;


        Path path = new Path();

        //vertical axis
        path.moveTo(p0x,p0y / 2.0f);
        path.lineTo(p0x,height - p0y / 2.0f);
        canvas.drawPath(path,mPaint);

        //horizontal axis
        path.reset();
        path.moveTo(centerX - triangleHeight,centerY);
        path.lineTo(centerX + triangleHeight,centerY);
        canvas.drawPath(path,mPaint);
        path.reset();


        // graduations
        float n = 1f;
        for(int i=0;i<11;i++) {
            path.reset();
            path.moveTo(centerX - triangleHeight * n, centerY - graduationHeight / 2);
            path.lineTo(centerX - triangleHeight * n, centerY + graduationHeight / 2);
            canvas.drawPath(path, mPaint);
            n -=  0.2f;
        }
        path.reset();

        //score left
        p1x = centerX - triangleHeight * scoreFactors.get(0).mScore * percent;
        path.reset();
        path.moveTo(p0x,p0y);
        path.lineTo(p1x,p1y);
        path.lineTo(p0x,p0y + triangleBase);
        path.close();
        gradient = new LinearGradient(p1x, p1y, centerX, centerY,  scoreFactors.get(0).mColor, 1 , Shader.TileMode.CLAMP);
        mPaintScore.setDither(true);
        mPaintScore.setAntiAlias(true);
        mPaintScore.setShader(gradient);
        canvas.drawPath(path,mPaintScore);

        //score right
        p1x = centerX + triangleHeight *  scoreFactors.get(1).mScore * percent;
        path.reset();
        path.moveTo(p0x,p0y);
        path.lineTo(p1x,p1y);
        path.lineTo(p0x,p0y + triangleBase);
        path.close();
        gradient = new LinearGradient(p1x, p1y, centerX, centerY,  scoreFactors.get(1).mColor, 1 , Shader.TileMode.CLAMP);
        mPaintScore.setDither(true);
        mPaintScore.setAntiAlias(true);
        mPaintScore.setShader(gradient);
        canvas.drawPath(path,mPaintScore);

        mPaintText.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(scoreFactors.get(0).mTitle,centerX - triangleHeight,centerY - triangleBase, mPaintText);

        mPaintText.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(scoreFactors.get(1).mTitle,centerX + triangleHeight,centerY - triangleBase, mPaintText);
    }


    private void drawCircle(Canvas canvas, float radius, float centerX, float centerY, Paint paintScore) {
        canvas.drawCircle(centerX,centerY,radius,paintScore);
    }

    private void drawPolygon(Canvas canvas,List<ScoreFactor> scoreFactors, int width, int height) {
        float radius;
        if (width > height) {
            radius = height * 0.3f;
        } else {
            radius = width * 0.3f;
        }

        float centerX = width /2.0f;
        float centerY = height / 2.0f;

        drawPolygon(canvas, scoreFactors, centerX, centerY, radius * 0.8f, false);
        drawPolygon(canvas, scoreFactors, centerX, centerY, radius * 0.6f, false);
        drawPolygon(canvas, scoreFactors, centerX, centerY, radius * 0.4f, false);

        drawPolygon(canvas, scoreFactors, centerX, centerY, radius, true);

    }

    private void drawPolygon(Canvas canvas, List<ScoreFactor> scoreFactors, float centerX, float centerY, float radius, boolean outer) {

        float angle = (float) (Math.PI * 2 / scoreFactors.size());
        float angleInner = angle / 2;
        //float innerRayon = (float) (radius * Math.cos(angle));
        float lengthSide = (float) (2.0f * radius * Math.sin(angleInner));
        float p0x = centerX;
        float p0y = centerY - radius;

        float p1x = (float) (p0x + Math.cos(-1 * angleInner) * lengthSide);
        float p1y = (float) (p0y - Math.sin(-1 * angleInner) * lengthSide);
        Path path = new Path();
        canvas.save();
        for (int i = 0 ; i < scoreFactors.size() ; ++i) {
            path.reset();
            if (!outer) {
                path.moveTo(p0x, p0y);
            } else {
                path.moveTo(centerX, centerY);
                path.lineTo(p0x, p0y);
            }
            path.lineTo(p1x, p1y);
            canvas.drawPath(path, mPaint);
            canvas.rotate((float) (-1 * angle * 180 / Math.PI), centerX, centerY);
        }
        canvas.restore();

        if (outer) {
            float pxScore;
            float pyScore;
            float px;
            float py;
            Path pathScore = null;

            float ang = (float) (-1 * Math.PI / 2);
            float factor;
            for (int i = 0 ; i < scoreFactors.size() ; ++i) {
                factor = scoreFactors.get(i).mScore * percent;
                px = (float) (centerX + Math.cos(ang) * radius);
                py = (float) (centerY + Math.sin(ang) * radius);
                pxScore = (float) (centerX + Math.cos(ang) * radius * factor);
                pyScore = (float) (centerY + Math.sin(ang) * radius * factor);
                if (pathScore == null) {
                    pathScore = new Path();
                    pathScore.moveTo(pxScore, pyScore);
                } else {
                    pathScore.lineTo(pxScore, pyScore);
                }
                int padding = 10;
                String title = scoreFactors.get(i).mTitle;
                Rect bounds = new Rect();
                float pxText;
                float pyText;
                if (title == null) {
                    title = "Default";
                }
                mPaintText.getTextBounds(title, 0, title.length(), bounds);
                if (centerX > px) {
                    mPaintText.setTextAlign(Paint.Align.RIGHT);
                    if ((int) centerY > (int)py) {
                        pxText = px - padding;
                        pyText = py - bounds.height() / 2;

                    } else if ((int)centerY < (int)py) {
                        pxText = px - padding;
                        pyText = py + bounds.height();
                    } else {
                        pxText = px - padding;
                        pyText = py + bounds.height() / 2;
                    }

                } else if ((int)centerX < (int)px) {

                    mPaintText.setTextAlign(Paint.Align.LEFT);
                    if ((int)centerY > (int)py) {
                        pxText = px + padding;
                        pyText = py - bounds.height() / 2;
                    } else if ((int)centerY < (int)py) {
                        pxText = px + padding;
                        pyText = py + bounds.height();
                    } else {
                        pxText = px + padding;
                        pyText = py + bounds.height() / 2;
                    }
                } else {
                    mPaintText.setTextAlign(Paint.Align.CENTER);
                    if ((int)centerY > (int)py) {
                        pxText = px;
                        pyText = py - bounds.height() / 2;
                    } else if ((int)centerY < (int)py) {
                        pxText = px;
                        pyText = py + bounds.height();
                    } else {
                        pxText = px;
                        pyText = py + bounds.height();
                    }
                }
                canvas.drawText(title, pxText, pyText, mPaintText);
                ang += angle;
            }
            if (pathScore != null) {
                pathScore.close();
                float[] stops = new float[scoreFactors.size() + 1];
                int[] colorsGradient = new int[scoreFactors.size() + 1];
                for (int i = 0; i < stops.length; ++i) {
                    stops[i] = (1.0f / scoreFactors.size()) * i;
                    if (i >= mScoreFactors.size()) {
                        colorsGradient[i] = scoreFactors.get(0).mColor;
                    } else {
                        colorsGradient[i] = scoreFactors.get(i).mColor;
                    }
                }

                SweepGradient gradient = new SweepGradient(centerX, centerY, colorsGradient, stops);
                Matrix matrix = new Matrix();
                matrix.setRotate(-90, centerX, centerY);
                gradient.setLocalMatrix(matrix);
                mPaintScore.setDither(true);
                mPaintScore.setAntiAlias(true);
                mPaintScore.setShader(gradient);
                canvas.drawPath(pathScore, mPaintScore);

            }

        }
    }

    /**
     * Set factor , Title and Colors
     */
    public void setScoreFactors(List<ScoreFactor> factors) {
        mScoreFactors = factors;
    }
}