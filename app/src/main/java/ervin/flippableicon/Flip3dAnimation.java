package ervin.flippableicon;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Flip3dAnimation  extends Animation {
    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private final float mDirectionDegrees;
    private Camera mCamera;

    public Flip3dAnimation(float fromDegrees, float toDegrees,
                           float centerX, float centerY) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
        mDirectionDegrees = 90;
    }

    public Flip3dAnimation(float fromDegrees, float toDegrees,
                           float centerX, float centerY, float directionDegrees) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
        mDirectionDegrees = directionDegrees;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final Camera camera = mCamera;
        final float directionDegrees = mDirectionDegrees;

        final Matrix matrix = t.getMatrix();

        camera.save();

        //camera.rotateX(degrees);
        camera.rotateY(degrees);
        //camera.rotateZ(degrees);



        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-centerX, -centerY);
        matrix.preRotate(directionDegrees, centerX, centerY);
        matrix.postRotate(-directionDegrees);
        matrix.postTranslate(centerX, centerY);

    }

}