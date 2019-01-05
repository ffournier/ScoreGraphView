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
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.List;

/**
 * Class ScoreGraphView
 * Display the score of a list of {@link ScoreFactor} with a graph
 */
public class ScoreGraphView extends View {

    // Variable Declaration
    private Paint mPaint;
    private Paint mPaintText;
    private Paint mPaintScore;


    // Data
    private List<ScoreFactor> mScoreFactors;
    private float percent = 0;
    private boolean isVisible = false;
    private ValueAnimator mAnimator;
    private static float sHeight_Max_Progress;


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
        float strokeWidth = 2.0f;
        int colorTitle = Color.BLACK;
        int colorLine = Color.BLACK;
        boolean animationEnabled = false;
        int animationDuration = 500;

        sHeight_Max_Progress = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ScoreGraphView, 0, 0);

            try {
                colorLine = a.getColor(R.styleable.ScoreGraphView_colorLine, Color.BLACK);
                colorTitle = a.getColor(R.styleable.ScoreGraphView_colorTitle, Color.BLACK);
                textSize = a.getDimensionPixelSize(R.styleable.ScoreGraphView_textSizeTitle, 20);
                strokeWidth = a.getFloat(R.styleable.ScoreGraphView_strokeWidth, 2.0f);
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
        mPaint.setStrokeWidth(strokeWidth);

        mPaintText = new Paint();
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setColor(colorTitle);
        mPaintText.setTextSize(textSize);

        mPaintScore = new Paint();
        mPaintScore.setAlpha(128);
        mPaintScore.setStyle(Paint.Style.FILL);


        // Animation
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
            if (mScoreFactors != null) {
                switch (mScoreFactors.size()) {
                    case 0:
                        break;
                    case 1:
                        // one dimension
                        drawProgressBarGraph(canvas, mScoreFactors.get(0), width, height);
                        break;
                    case 2:
                        // two dimension
                        drawDiamondGraph(canvas,mScoreFactors ,width, height);
                        break;
                    default:
                        // x dimension
                        drawPolygon(canvas, mScoreFactors, width, height);
                        break;
                }
            }
            // out of screen
            if (!isVisible && mAnimator != null) {
                mAnimator.start();
            }
            isVisible = true;

        } else {
            // refresh all 50 ms
            isVisible = false;
            postInvalidateDelayed(50);
        }

    }

    /**
     * add Dark or Light in color
     * @param color : the base color
     * @param dark : darkness or lightness
     * @return the new color
     */
    private int changeDarknessIntToColor(int color, boolean dark) {
        int r =  (( color >> 16) & 0xff);
        int g =  (( color >> 8) & 0xff);
        int b =  (( color     ) & 0xff) ;
        int a =  (( color >> 24) & 0xff);
        float[] hsv = new float[3];
        // get the color in hsv
        Color.colorToHSV(color, hsv);
        if (dark) {
            // treat value
            hsv[2] = hsv[2] - 0.3f;
        } else {
            // treat saturation
            hsv[1] = hsv[1] - 0.3f;
        }
        return Color.HSVToColor(a, hsv);
    }

    /**
     * Draw ScoreFactor for one dimension
     * @param canvas : the canvas to draw
     * @param scoreFactor : the scoreFactor to apply
     * @param width : the width of drawing
     * @param height : the height of drawing
     */
    private void drawProgressBarGraph(Canvas canvas, ScoreFactor scoreFactor, int width, int height) {

        // calculate center of graph
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        float widthRect = width * 0.8f;
        float heightRect = height * 0.2f;
        if (heightRect > sHeight_Max_Progress) {
            heightRect = sHeight_Max_Progress;
        }

        float radius = 0.5f;
        float margin = mPaint.getStrokeWidth() * 3;

        float left = centerX - widthRect / 2.0f + margin;
        float factor = scoreFactor.mScore * percent ;
        float right = left +  (widthRect - margin) * factor;

        // Draw Progress Bar inner
        RectF rectScore = new RectF(left , centerY - heightRect / 2.0f + margin ,
                right,centerY + heightRect / 2.0f - margin );
        LinearGradient gradient = new LinearGradient(rectScore.left, rectScore.top, rectScore.right, rectScore.bottom,
                changeDarknessIntToColor(scoreFactor.mColor, false), scoreFactor.mColor , Shader.TileMode.CLAMP);
        mPaintScore.setDither(true);
        mPaintScore.setAntiAlias(true);
        mPaintScore.setShader(gradient);
        canvas.drawRoundRect(rectScore,heightRect * radius, heightRect * radius,  mPaintScore);

        // Draw Progress Bar outer
        RectF rect = new RectF(centerX - widthRect / 2.0f, centerY - heightRect / 2.0f,
                centerX + widthRect / 2.0f , centerY + heightRect / 2.0f);
        canvas.drawRoundRect(rect, heightRect * radius, heightRect * radius,  mPaint);

        // Draw Title
        mPaintText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(scoreFactor.mTitle,centerX,centerY - heightRect, mPaintText);
    }

    /**
     * Draw 2 dimension Graph
     * @param canvas : the canvas to draw
     * @param scoreFactors : the scoreFactors to apply
     * @param width : the width of drawing
     * @param height : the height of drawing
     */
    private void drawDiamondGraph(Canvas canvas, List<ScoreFactor> scoreFactors, int width, int height) {
        // calculate the center of graph
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        float triangleHeight =  width / 2.0f * 0.8f;
        float triangleBase = height / 2.0f * 0.5f;
        float graduationHeight = height / 30.0f;

        float p0x = centerX;
        float p0y = centerY - triangleBase / 2.0f;
        float p1y = centerY;

        Path path = new Path();

        //vertical axis
        path.moveTo(p0x, centerY * 2 / 3.f);
        path.lineTo(p0x, centerY * 4 / 3.f);
        canvas.drawPath(path,mPaint);

        //horizontal axis
        path.reset();
        path.moveTo(centerX - triangleHeight, centerY);
        path.lineTo(centerX + triangleHeight, centerY);
        canvas.drawPath(path,mPaint);
        path.reset();

        // graduations
        float n = 1f;
        final int NB_GRADUATION = 10;
        final float GAP_GRADUATION = (1.0f - -1.0f) / NB_GRADUATION;
        for(int i = 0 ; i <= NB_GRADUATION ; ++i) {
            path.reset();
            path.moveTo(centerX - triangleHeight * n, centerY - graduationHeight / 2);
            path.lineTo(centerX - triangleHeight * n, centerY + graduationHeight / 2);
            canvas.drawPath(path, mPaint);
            n -= GAP_GRADUATION;
        }

        //score left
        drawTriangleDiamond(canvas, centerX, centerY, p0x, centerX - triangleHeight * scoreFactors.get(0).mScore * percent,
                p0y, p1y, centerX - triangleHeight, centerY - triangleBase * 1 / 3, triangleBase,
                Paint.Align.LEFT, scoreFactors.get(0));
        //score right
        drawTriangleDiamond(canvas, centerX, centerY, p0x, centerX + triangleHeight * scoreFactors.get(1).mScore * percent,
                p0y, p1y, centerX + triangleHeight, centerY - triangleBase * 1 / 3, triangleBase,
                Paint.Align.RIGHT, scoreFactors.get(1));
    }


    private void drawTriangleDiamond(Canvas canvas, float centerX, float centerY,
                                     float p0x, float p1x, float p0y, float p1y,
                                     float p2x, float p2y, float triangleBase,
                                     Paint.Align align, ScoreFactor factor) {
        Path path = new Path();
        //score left
        path.moveTo(p0x, p0y);
        path.lineTo(p1x, p1y);
        path.lineTo(p0x, p0y + triangleBase);
        path.close();
        LinearGradient gradient = new LinearGradient(p1x, p1y, centerX, centerY,  changeDarknessIntToColor(factor.mColor, true),
                factor.mColor, Shader.TileMode.CLAMP);
        mPaintScore.setDither(true);
        mPaintScore.setAntiAlias(true);
        mPaintScore.setShader(gradient);
        canvas.drawPath(path, mPaintScore);

        // Draw Title Left
        mPaintText.setTextAlign(align);
        canvas.drawText(factor.mTitle, p2x, p2y, mPaintText);
    }

    /**
     * Draw Multi Dimension of scoreFactors
     * @param canvas  the canvas to draw
     * @param scoreFactors : the scoreFactors to display
     * @param width : the with of drawing
     * @param height : the height of drawing
     */
    private void drawPolygon(Canvas canvas,List<ScoreFactor> scoreFactors, int width, int height) {
        float radius;
        if (width > height) {
            radius = height * 0.3f;
        } else {
            radius = width * 0.3f;
        }

        float centerX = width /2.0f;
        float centerY = height / 2.0f;

        // draw inner line
        drawPolygon(canvas, scoreFactors, centerX, centerY, radius * 0.8f, false);
        drawPolygon(canvas, scoreFactors, centerX, centerY, radius * 0.6f, false);
        drawPolygon(canvas, scoreFactors, centerX, centerY, radius * 0.4f, false);

        // draw score factors
        drawPolygon(canvas, scoreFactors, centerX, centerY, radius, true);

    }

    /**
     * Draw Polygon of X dimensions
     * @param canvas : the canvas to draw
     * @param scoreFactors : the score factors to display
     * @param centerX : the center in axe X
     * @param centerY : the center in axe Y
     * @param radius : the radius of circle
     * @param outer : if inner ou score graph
     */
    private void drawPolygon(Canvas canvas, List<ScoreFactor> scoreFactors, float centerX, float centerY, float radius, boolean outer) {

        //
        float angle = (float) (Math.PI * 2 / scoreFactors.size());
        float angleInner = angle / 2;
        //float innerRayon = (float) (radius * Math.cos(angle));
        float lengthSide = (float) (2.0f * radius * Math.sin(angleInner));
        float p0x = centerX;
        float p0y = centerY - radius;

        float p1x = (float) (p0x + Math.cos(-1 * angleInner) * lengthSide);
        float p1y = (float) (p0y - Math.sin(-1 * angleInner) * lengthSide);

        // draw lines of score or inner line of graph
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

        // if outer (score graph)
        if (outer) {
            float pxScore;
            float pyScore;
            float px;
            float py;
            Path pathScore = null;

            float ang = (float) (-1 * Math.PI / 2);
            float factor;
            // draw title of score graph
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
                // fill color of graph
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