package ervin.flippableicon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;

public class FlippableIcon extends View {
    private final static int OUTER_CIRCLE_DIAMETER = 226; // TODO: value should change according to resolution
    private final static int OUTER_CIRCLE_SPACING = 8;
    private final static int OUTER_CIRCLE_STROKE_WIDTH = 4;
    private final static int OUTER_CIRCLE_RADIUS = OUTER_CIRCLE_DIAMETER /2;
    private final static int INNER_CIRCLE_RADIUS = OUTER_CIRCLE_RADIUS - OUTER_CIRCLE_SPACING;
    private final static int ACTUAL_BOUND_RADIUS = OUTER_CIRCLE_STROKE_WIDTH/2 + OUTER_CIRCLE_RADIUS;
    private final static int CIRCLE_CENTRAL_X = ACTUAL_BOUND_RADIUS;
    private final static int CIRCLE_CENTRAL_Y = ACTUAL_BOUND_RADIUS;
    private final static int MOVE_START = 11;
    private final static int MOVE_END = 12;

    private Bitmap Image1;
    private Bitmap Image2;
    private boolean enable_flipped = true;
    private boolean enable_double_click = true;
    private boolean double_click_on = false;
    private boolean flipped = false;
    /*private String detail_info = "";
    private float msg_width;
    private float msg_height;*/
    Paint circle_paint;
    Paint msg_paint;
    private MoveHandler move_handler = new MoveHandler();

    static private ViewCallback view_callback;
    public interface ViewCallback{
        public void onTouch();
    }
    static public void setCallback(ViewCallback Vcb) {
        view_callback = Vcb;
    }

    static public void removeCallback() {
        view_callback = null;
    }

    public void setConfig(Double speed, Double factor) {
        if (factor != null) {
            move_handler.setFactor(factor);
        }
        if (speed != null) {
            move_handler.setSpeed(speed);
        }
    }

    public void setDoubleClickBound(float percent) {
        move_handler.setDoubleClickBound(percent);
    }

    // for text layout
    /*public void setDetailInfo(String info) {
        msg_paint.setColor(Color.BLACK);
        msg_paint.setTextSize(18);
        detail_info = info;
        msg_width = msg_paint.measureText(detail_info) / 2;
        msg_height = msg_paint.getTextSize()/2;
    }*/

    public FlippableIcon(Context context, AttributeSet attrs) {
        super(context, attrs);

        doubleClickDetector = new DoubleClickListener() {
            @Override
            public void onSingleClick(Float X, Float Y) { }

            @Override
            public void onDoubleClick(Float X, Float Y) {
                if (!enable_flipped|| !enable_double_click || move_handler.move_on) return;
                enable_double_click = false;
                double_click_on = true;
                lastClickTime = 0;
                final Flip3dAnimation in;
                final Flip3dAnimation out;
                Float direction = (float) Math.toDegrees(Math.atan2(Y - CIRCLE_CENTRAL_Y, X - CIRCLE_CENTRAL_X));
                if (direction >= 0) {
                    direction = 180 - direction;
                    out = new Flip3dAnimation(0, -90, CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, direction);
                    in = new Flip3dAnimation(90, 0, CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, direction);
                } else {
                    direction = -direction;
                    out = new Flip3dAnimation(0, 90, CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, direction);
                    in = new Flip3dAnimation(-90, 0, CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, direction);
                }

                out.setDuration(200);
                out.setInterpolator(new AccelerateDecelerateInterpolator());
                in.setDuration(200);
                in.setInterpolator(new OvershootInterpolator(1.5f));
                out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // OnDraw the text now
                        enable_double_click = true;
                        flipped = !flipped;
                        FlippableIcon.this.startAnimation(in);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        double_click_on = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                FlippableIcon.this.startAnimation(out);
            }
        };
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlippableIcon, 0, 0);
        Integer ImageValue1 = null;
        Integer ImageValue2 = null;
        circle_paint = new Paint();
        msg_paint = new Paint();
        try{
            ImageValue1 = typedArray.getResourceId(R.styleable.FlippableIcon_Image1, 0);
            ImageValue2 = typedArray.getResourceId(R.styleable.FlippableIcon_Image2, 0);
        }
        finally {
            typedArray.recycle();
        }
        Image1 = BitmapFactory.decodeResource(getResources(), ImageValue1);
        Image2 = BitmapFactory.decodeResource(getResources(), ImageValue2);
        double scale_rate1 = Math.max((double) CIRCLE_CENTRAL_Y / Image1.getHeight(), (double) CIRCLE_CENTRAL_X / Image1.getWidth()) * 1.5;
        double scale_rate2 = Math.max((double) CIRCLE_CENTRAL_Y / Image2.getHeight(), (double) CIRCLE_CENTRAL_X / Image2.getWidth()) * 1.5;
        // resize the image
        Image1 = Bitmap.createScaledBitmap(Image1, (int) (Image1.getWidth() * scale_rate1),(int) (Image1.getHeight() * scale_rate1), false);
        Image2 = Bitmap.createScaledBitmap(Image2, (int) (Image2.getWidth() * scale_rate2),(int) (Image2.getHeight() * scale_rate2), false);
        //setDetailInfo(info);
    }

    DoubleClickListener doubleClickDetector;

    public abstract class DoubleClickListener{

        private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds
        private static final double DOUBLE_CLICK_DISTANCE_DELTA = 40;
        private static final long LONG_PRESS_TIME_DELTA = 1000;


        public void refresh() {
            lastClickTime = 0;
            lastPosX = lastPosY = null;
        }

        public void pause() {
            paused = true;
        }

        public void resume() {
            paused = false;
        }

        private boolean paused = false;
        long lastClickTime = 0;
        Float lastPosX = null;
        Float lastPosY = null;

        public void onClick(Float X, Float Y) {
            if (paused) return;
            long clickTime = System.currentTimeMillis();
            // every 3-click just return one double click
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                Double distance = Math.hypot(X- lastPosX, Y - lastPosY);
                //Log.d("ERVIN", "distance is " + distance);
                if (distance < DOUBLE_CLICK_DISTANCE_DELTA) {
                    onDoubleClick(X, Y);
                    return;
                }
            }
            lastPosX = X;
            lastPosY = Y;
            lastClickTime = clickTime;
            onSingleClick(X, Y);
        }

        public abstract void onSingleClick(Float X, Float Y);
        public abstract void onDoubleClick(Float X, Float Y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!enable_flipped || double_click_on) return false;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Message start_msg = new Message();
                start_msg.what = MOVE_START;
                Bundle mBundle = new Bundle();
                mBundle.putFloat("DISTANCE_Y", e.getY() - CIRCLE_CENTRAL_Y);
                mBundle.putFloat("DISTANCE_X", e.getX() - CIRCLE_CENTRAL_X);
                start_msg.setData(mBundle);
                move_handler.sendMessage(start_msg);
                if (view_callback != null) {
                    view_callback.onTouch();
                }
                doubleClickDetector.onClick(e.getX(), e.getY());
                break;
            case MotionEvent.ACTION_UP:
                move_handler.sendEmptyMessage(MOVE_END);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!move_handler.start_handle) {
                    break;
                }
                float distance_y = e.getY() - CIRCLE_CENTRAL_Y;
                float distance_x = e.getX() - CIRCLE_CENTRAL_X;
                Message msg = new Message();
                Bundle pos_bundle = new Bundle();
                pos_bundle.putFloat("DISTANCE_Y", distance_y);
                pos_bundle.putFloat("DISTANCE_X", distance_x);
                msg.setData(pos_bundle);
                move_handler.sendMessage(msg);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ACTUAL_BOUND_RADIUS * 2, ACTUAL_BOUND_RADIUS * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        circle_paint.setAntiAlias(true);
        circle_paint.setStyle(Paint.Style.STROKE);
        circle_paint.setStrokeWidth(3);
        circle_paint.setColor(Color.BLACK);
        canvas.drawCircle(CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, OUTER_CIRCLE_RADIUS, circle_paint);
        circle_paint.setStrokeWidth(2);
        canvas.drawCircle(CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, INNER_CIRCLE_RADIUS,circle_paint);
        if (flipped) {
            if (Image2 != null) {
                canvas.drawBitmap(Image2, CIRCLE_CENTRAL_X - (Image2.getWidth() / 2), CIRCLE_CENTRAL_Y - (Image2.getHeight() / 2), null);
            }
        } else {
            if (Image1 != null) {
                canvas.drawBitmap(Image1, CIRCLE_CENTRAL_X - (Image1.getWidth() / 2), CIRCLE_CENTRAL_Y - (Image1.getHeight() / 2), null);
            }
        }
    }

    class MoveHandler extends Handler {

        float mFactor = 40;
        float mSpeed = (float) 2.5;
        float mClickBound = 2;

        void setFactor(double factor) {
            mFactor = (float) factor;
        }

        void setSpeed(double speed) {
            mSpeed = (float) speed;
        }

        void setDoubleClickBound(float bound) {
            mClickBound = bound;
        }


        final int MAX_SAMPLE_NUM = 5;

        boolean start_handle = false;
        boolean slide_up = true;
        private Float last_pos_y = (float) 0;
        private Float last_pos_x = (float) 0;
        private Float start_pos_y  = (float) 0;
        private Float start_pos_x = (float) 0;
        private Float cur_angle = 0f;
        private Float percent = (float) 0;
        Flip3dAnimation rotateAnimation;
        boolean isChange_layout = false;
        private Float direction_angle = null;
        private final int frame_interval = 17;
        private int sample_collect_times = 0;
        public boolean move_on = false;

        private boolean need_change(Float pre_angle, Float new_angle) {
            Float pre = pre_angle % 180;
            Float post = new_angle % 180;
            if (pre_angle < new_angle) {
                if (pre <= 90 && post > 90) {
                    return true;
                }
            } else {
                if (pre > 90 && post <= 90) {
                    return true;
                }
            }
            return false;

        }

        private Float calculate_direction(Float Y, Float X) {
            Float direction = (float) Math.toDegrees(Math.atan2(Y, X));
            if (direction >= 0) {
                direction = 180 - direction;
            } else {
                direction = -direction;
            }
            return direction;
        }

        public void clear() {
            direction_angle = null;
            cur_angle = 0f;
            percent = 0f;
            isChange_layout = false;
            sample_collect_times = 0;
        }

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MOVE_START:
                    if (double_click_on) break;
                    start_handle = true;
                    Bundle start_bundle = msg.getData();
                    clear();
                    move_on = true;
                    last_pos_y = start_bundle.getFloat("DISTANCE_Y");
                    last_pos_x = start_bundle.getFloat("DISTANCE_X");
                    start_pos_y = last_pos_y;
                    start_pos_x = last_pos_x;
                    if (start_pos_y > 0) {
                        slide_up = true;
                    } else {
                        slide_up = false;
                    }
                    //Log.d("direction", "start pos is " + start_pos_x + ", " + start_pos_y);
                    break;
                case MOVE_END:
                    if (double_click_on || direction_angle == null) break;
                    if (isChange_layout) {
                        Integer rotate_angle = 0;
                        if (cur_angle >= -90) {
                            rotate_angle = (Math.round(cur_angle + 90) / 180) * 180 - 180;
                        } else {
                            rotate_angle = (Math.round(cur_angle - 90) / 180) * 180 - 180;
                        }
                        //Log.d("ANGLE", "rotate angle is " + rotate_angle + " true angle is " + cur_angle);
                        rotateAnimation = new Flip3dAnimation(cur_angle - 180, rotate_angle, CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, direction_angle);
                        rotateAnimation.setDuration(300);
                        rotateAnimation.setInterpolator(new OvershootInterpolator(Math.abs(Math.round(cur_angle)) / 50));
                        rotateAnimation.setRepeatCount(0);
                        rotateAnimation.setFillEnabled(false);
                        //rotateAnimation.setFillAfter(true);
                        FlippableIcon.this.startAnimation(rotateAnimation);
                    } else {
                        Integer rotate_angle = 0;
                        if (cur_angle >= -90) {
                            rotate_angle = (Math.round(cur_angle + 90) / 360) * 360;
                        } else {
                            rotate_angle = (Math.round(cur_angle - 90) / 360) * 360;
                        }
                        //Log.d("ANGLE", "rotate angle is " + cur_angle + " true angle is " + rotate_angle);
                        rotateAnimation = new Flip3dAnimation(cur_angle, rotate_angle, CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, direction_angle);
                        rotateAnimation.setDuration(500);
                        rotateAnimation.setInterpolator(new Interpolator() {
                            @Override
                            public float getInterpolation(float input) {
                                // damp function
                                double x = (double) input;
                                return (float) (1 - Math.pow(Math.E, -5 * x) * Math.cos(11 * x));
                            }
                        });
                        rotateAnimation.setRepeatCount(0);
                        rotateAnimation.setFillEnabled(false);
                        //rotateAnimation.setFillAfter(true);
                        FlippableIcon.this.startAnimation(rotateAnimation);
                    }
                    clear();
                    move_on = false;
                    start_handle = false;
                    break;
                default:
                    if (double_click_on) break;
                    Bundle bundle = msg.getData();
                    Float cur_pos_y = bundle.getFloat("DISTANCE_Y");
                    Float cur_pos_x = bundle.getFloat("DISTANCE_X");
                    if (direction_angle == null) {
                        direction_angle = calculate_direction(cur_pos_y - last_pos_y, cur_pos_x - last_pos_x);
                        //Log.d("Direction", "direction is " + instant_direction);
                    } else if (sample_collect_times <= MAX_SAMPLE_NUM) {
                        direction_angle = (direction_angle * sample_collect_times
                                + calculate_direction(cur_pos_y - last_pos_y, cur_pos_x - last_pos_x))
                                / (sample_collect_times + 1);
                        ++ sample_collect_times;
                    }
                    Float average_direction = calculate_direction(cur_pos_y - start_pos_y, cur_pos_x - start_pos_x);
                    //Log.d("Direction", "instant angle is " + calculate_direction(cur_pos_y - last_pos_y, cur_pos_x - last_pos_x));
                    //Log.d("Direction", "average angle is " + average_direction);
                    Float temp_per = (float) Math.hypot(start_pos_x - cur_pos_x, start_pos_y - cur_pos_y);
                    //Log.d("Direction", "angle is " + Math.abs(direction_angle - average_direction));
                    //Log.d("Direction1", temp_per.toString());
                    temp_per = (float) (temp_per * Math.cos(Math.toRadians(Math.abs(direction_angle - average_direction))));
                    if (Math.abs(temp_per) > mClickBound) {
                        doubleClickDetector.refresh();
                        Log.d("ERVIN", "刷新");
                    }
                    last_pos_y = cur_pos_y;
                    last_pos_x = cur_pos_x;
                    if (cur_pos_y > start_pos_y) temp_per = -temp_per;
                    //Log.d("Direction2", temp_per.toString())

                    // TODO: factor should change according to resolution
                    if (temp_per >= 0) {
                        percent = (float) (Math.log(temp_per + mFactor)  - Math.log(mFactor)) / mSpeed;
                    } else {
                        percent = -(float) (Math.log(-temp_per + mFactor) - Math.log(mFactor)) / mSpeed;
                    }
                    if (need_change(Math.abs(cur_angle), Math.abs(180 * percent))) {
                        flipped = !flipped;
                        isChange_layout = !isChange_layout;
                        invalidate();
                    }
                    if (isChange_layout) {
                        rotateAnimation = new Flip3dAnimation(cur_angle - 180, percent * 180 - 180, CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, direction_angle);
                    } else {
                        rotateAnimation = new Flip3dAnimation(cur_angle, percent * 180, CIRCLE_CENTRAL_X, CIRCLE_CENTRAL_Y, direction_angle);
                    }
                    cur_angle = percent * 180;
                    rotateAnimation.setDuration(frame_interval);
                    //rotateAnimation.setInterpolator(new LinearInterpolator());
                    rotateAnimation.setRepeatCount(0);
                    rotateAnimation.setFillEnabled(true);
                    rotateAnimation.setFillAfter(true);
                    FlippableIcon.this.startAnimation(rotateAnimation);
                    break;
            }
        }
    };


}
