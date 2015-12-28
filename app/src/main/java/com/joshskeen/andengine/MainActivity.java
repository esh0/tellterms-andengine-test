package com.joshskeen.andengine;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener, IUpdateHandler,
        ScrollDetector.IScrollDetectorListener {
    // ===========================================================
    // Constants
    // ===========================================================

    protected int CAMERA_WIDTH;
    protected int CAMERA_HEIGHT;

    // ===========================================================
    // Fields
    // ===========================================================

    private Scene mScene;
    protected PhysicsWorld mPhysicsWorld;
    private HashMap<String, Body> bodies = new HashMap<>();

    private BitmapTextureAtlas mCircleActiveBitmapTextureAtlas;
    private BitmapTextureAtlas mCircleLargeBitmapTextureAtlas;
    private BitmapTextureAtlas mCircleMediumBitmapTextureAtlas;
    private BitmapTextureAtlas mCircleSmallBitmapTextureAtlas;

    private TextureRegion mCirlceActiveTextureRegion;
    private TextureRegion mCircleLargeTextureRegion;
    private TextureRegion mCircleMediumTextureRegion;
    private TextureRegion mCircleSmallTextureRegion;
    private BitmapTextureAtlas mDroidFontTexture;
    private Font mDroidFont;
    private Body mCenterBody;
    private SurfaceScrollDetector mScrollDetector;
    private Camera mCamera;
    private float mMinY = 0;
    private float mMaxY = 0;
    private float mCurrentY = 0;
    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public EngineOptions onCreateEngineOptions() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        CAMERA_HEIGHT = metrics.heightPixels;
        CAMERA_WIDTH = metrics.widthPixels;
        mMaxY = CAMERA_HEIGHT * 2;
        this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        return new EngineOptions(false, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        FontFactory.setAssetBasePath("font/");

        this.mCircleActiveBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 384, 384, TextureOptions.BILINEAR);
        this.mCircleLargeBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
        this.mCircleMediumBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 196, 196, TextureOptions.BILINEAR);
        this.mCircleSmallBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 144, 144, TextureOptions.BILINEAR);
        this.mDroidFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        this.mCirlceActiveTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCircleActiveBitmapTextureAtlas, this, "circle_green_active.png", 0, 0); // 64x32
        this.mCircleLargeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCircleLargeBitmapTextureAtlas, this, "circle_green_full.png", 0, 0); // 64x32
        this.mCircleMediumTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCircleMediumBitmapTextureAtlas, this, "circle_green_outline.png", 0, 0); // 64x32
        this.mCircleSmallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCircleSmallBitmapTextureAtlas, this, "circle_orange_full.png", 0, 0); // 64x32
        this.mDroidFont = FontFactory.createFromAsset(getFontManager(), this.mDroidFontTexture, getAssets(), "Droid.ttf", 24, true, Color.WHITE);

        this.mCircleActiveBitmapTextureAtlas.load();
        this.mCircleLargeBitmapTextureAtlas.load();
        this.mCircleMediumBitmapTextureAtlas.load();
        this.mCircleSmallBitmapTextureAtlas.load();
        this.mDroidFontTexture.load();
        this.mDroidFont.load();
    }

    @Override
    public Scene onCreateScene() {
        this.mEngine.registerUpdateHandler(new FPSLogger());
        this.mEngine.registerUpdateHandler(this);

        this.mScene = new Scene();
        this.mScene.setBackground(new Background(1f, 1f, 1f));

        this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);

        final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
        final Rectangle ground = new Rectangle(0, mMaxY - 1, CAMERA_WIDTH, 1, vertexBufferObjectManager);
        final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 1, vertexBufferObjectManager);
        final Rectangle left = new Rectangle(0, 0, 1, mMaxY, vertexBufferObjectManager);
        final Rectangle right = new Rectangle(CAMERA_WIDTH - 1, 0, 1, mMaxY, vertexBufferObjectManager);

        final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyDef.BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyDef.BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyDef.BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyDef.BodyType.StaticBody, wallFixtureDef);

        this.mScene.attachChild(ground);
        this.mScene.attachChild(roof);
        this.mScene.attachChild(left);
        this.mScene.attachChild(right);

        final FixtureDef gravityFixtureDef = PhysicsFactory.createFixtureDef(1, 0, 0);
        this.mCenterBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld,
                CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2,
                1,
                BodyDef.BodyType.StaticBody,
                gravityFixtureDef);

        this.mScene.registerUpdateHandler(this.mPhysicsWorld);
        this.mScene.setTouchAreaBindingOnActionMoveEnabled(true);
        this.mScene.setTouchAreaBindingOnActionDownEnabled(true);

        int count = 20;
        Random r = new Random();
        for (int i = 0; i< count; i++) {
            addCircle(r.nextInt(CAMERA_WIDTH - 384), r.nextInt(CAMERA_HEIGHT - 384));
        }

        this.mScrollDetector = new SurfaceScrollDetector(this);

        this.mScene.setOnSceneTouchListener(this);
        this.mScene.setTouchAreaBindingOnActionMoveEnabled(true);
        this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
        this.mScene.setOnSceneTouchListenerBindingOnActionDownEnabled(true);

        return this.mScene;
    }

    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
        return true;
    }

    @Override
    public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

    }

    @Override
    public void onAccelerationChanged(final AccelerationData pAccelerationData) {

    }

    @Override
    public void onResumeGame() {
        super.onResumeGame();
    }

    @Override
    public void onPauseGame() {
        super.onPauseGame();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void addCircle(final float pX, final float pY) {
        Sprite sprite;
        final Body body;

        final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0, 1);

        ITextureRegion textureRegion;
        TextOptions options = new TextOptions();
        options.setHorizontalAlign(HorizontalAlign.CENTER);
        Text text = new Text(pX, pY, mDroidFont,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                options, this.getVertexBufferObjectManager());

        Random r = new Random();
        int rr = r.nextInt(3);
        if (rr < 1) {
            textureRegion = mCircleLargeTextureRegion;
            text.setColor(1f, 1f, 1f);
        } else if (rr < 2) {
            textureRegion = mCircleMediumTextureRegion;
            text.setColor(0f, 0f, 0f);
        } else {
            textureRegion = mCircleSmallTextureRegion;
            text.setColor(1f, 1f, 1f);
        }

        sprite = getSprite(pX, pY, objectFixtureDef, textureRegion);

        if (text.getWidth() > sprite.getWidth()) {
            float maxWidth = sprite.getWidth();
            float currentWidth = 0;
            do {
                CharSequence orgText = text.getText().toString().replace("...", "");
                text.setText(TextUtils.concat(orgText.subSequence(0, orgText.length() - 1), "..."));
                currentWidth = text.getWidth();
            } while (currentWidth > maxWidth);
        }

        text.setPosition((sprite.getWidth() - text.getWidth()) / 2, (sprite.getHeight() - text.getHeight()) / 2);
        sprite.attachChild(text);

        body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, sprite, BodyDef.BodyType.DynamicBody, objectFixtureDef);
        body.setFixedRotation(true);
        bodies.put(((UserData)sprite.getUserData()).getUuid(), body);
        this.mScene.registerTouchArea(sprite);
        this.mScene.attachChild(sprite);
        this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(sprite, body, true, true));
    }

    @NonNull
    private Sprite getSprite(final float pX, final float pY, final FixtureDef objectFixtureDef, ITextureRegion textureRegion) {
        Sprite face;
        face = new Sprite(pX, pY, textureRegion, this.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                int eventaction = pSceneTouchEvent.getAction();

                float X = pSceneTouchEvent.getX();
                float Y = pSceneTouchEvent.getY();
                PhysicsConnector connector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(this);
                Body body = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(this);

                switch (eventaction) {
                    case TouchEvent.ACTION_DOWN:
                        break;
                    case TouchEvent.ACTION_MOVE:
                        ((UserData)this.getUserData()).setMoving(true);
                        return false;
                    case TouchEvent.ACTION_UP:
                        if (((UserData)this.getUserData()).isMoving()) {
                            ((UserData)this.getUserData()).setMoving(false);
                            return false;
                        }

                        if (X < getX() || X > getX() + getWidth() || Y < getY() || Y > getY() + getHeight())
                            return false;

                        Sprite newSprite;
                        Body newBody;
                        ITextureRegion textureRegion;
                        if (((UserData)this.getUserData()).getImageSize() == 256) {
                            textureRegion = mCircleLargeTextureRegion;
                        } else if (((UserData)this.getUserData()).getImageSize() == 196) {
                            textureRegion = mCircleMediumTextureRegion;
                        } else {
                            textureRegion = mCircleSmallTextureRegion;
                        }

                        if (((UserData)this.getUserData()).isChecked()) {
                            newSprite = getSprite(
                                    this.getX() + (mCircleLargeTextureRegion.getWidth() - ((UserData)this.getUserData()).getImageSize()/2),
                                    this.getY() + (mCircleLargeTextureRegion.getWidth() - ((UserData)this.getUserData()).getImageSize()/2),
                                    objectFixtureDef,
                                    textureRegion);
                            newSprite.setUserData(this.getUserData());
                            ((UserData)newSprite.getUserData()).setChecked(false);

                            newBody = PhysicsFactory.createCircleBody(mPhysicsWorld, newSprite, BodyDef.BodyType.DynamicBody, objectFixtureDef);
                        } else {
                            newSprite = getSprite(
                                    this.getX() - (mCircleLargeTextureRegion.getWidth() - ((UserData)this.getUserData()).getImageSize()/2),
                                    this.getY() - (mCircleLargeTextureRegion.getWidth() - ((UserData)this.getUserData()).getImageSize()/2),
                                    objectFixtureDef,
                                    mCirlceActiveTextureRegion);
                            newSprite.setUserData(this.getUserData());
                            ((UserData)newSprite.getUserData()).setChecked(true);

                            newBody = PhysicsFactory.createCircleBody(mPhysicsWorld, newSprite, BodyDef.BodyType.StaticBody, objectFixtureDef);
                        }

                        synchronized (bodies) {
                            bodies.put(((UserData) newSprite.getUserData()).getUuid(), newBody);
                        }
                        mPhysicsWorld.unregisterPhysicsConnector(connector);
                        mScene.detachChild(this);
                        mScene.unregisterTouchArea(this);
                        mPhysicsWorld.destroyBody(body);

                        mScene.registerTouchArea(newSprite);
                        mScene.attachChild(newSprite);
                        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(newSprite, newBody, true, true));
                        break;
                }
                return true;
            }
        };
        UserData userData = new UserData();
        userData.setUuid(UUID.randomUUID().toString());
        if (textureRegion == mCircleLargeTextureRegion) {
            userData.setImageSize(256);
        } else if (textureRegion == mCircleMediumTextureRegion) {
            userData.setImageSize(196);
        } else {
            userData.setImageSize(144);
        }

        face.setUserData(userData);
        return face;
    }

    class UserData implements Serializable {
        private String uuid;
        private boolean moving;
        private boolean checked;
        private int imageSize;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public boolean isMoving() {
            return moving;
        }

        public void setMoving(boolean isMoving) {
            this.moving = isMoving;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public int getImageSize() {
            return imageSize;
        }

        public void setImageSize(int imageSize) {
            this.imageSize = imageSize;
        }
    }

    @Override
    public void onUpdate(float pSecondsElapsed) {
        this.mPhysicsWorld.clearForces();
        synchronized (bodies) {
            for (Body body : bodies.values()) {
                Vector2 currentBodyPosition = body.getWorldCenter();
                //CircleShape planetShape = (CircleShape) gravityBody.getFixtureList().get(0).getShape();
                //float planetRadius = 8000;//planetShape.getRadius();
                float distanceToNearestBody = Float.MAX_VALUE;
                Vector2 positionToNearestBody = new Vector2(0, 0);
                for (Body otherBody : bodies.values()) {
                    Vector2 otherBodyPosition = otherBody.getWorldCenter();
                    Vector2 positionToOtherBody = new Vector2(0, 0);
                    positionToOtherBody.add(currentBodyPosition);
                    positionToOtherBody.sub(otherBodyPosition);
                    if (positionToOtherBody.len() != 0 && positionToOtherBody.len() < distanceToNearestBody) {
                        distanceToNearestBody = positionToOtherBody.len();
                        positionToNearestBody = new Vector2(positionToOtherBody.x, positionToOtherBody.y);
                    }
                }

                if (distanceToNearestBody > 10)
                    positionToNearestBody.negativeSelf();

                float vecSum = Math.abs(positionToNearestBody.x) + Math.abs(positionToNearestBody.y);
                positionToNearestBody.mul(vecSum / distanceToNearestBody);
                body.applyForce(positionToNearestBody, body.getWorldCenter());

                Vector2 distanceToCenter = new Vector2(0, 0);
                distanceToCenter.add(currentBodyPosition);
                distanceToCenter.sub(mCenterBody.getWorldCenter());
                distanceToCenter.negativeSelf();
                body.applyForce(distanceToCenter, body.getWorldCenter());
            }
        }
    }

    @Override
    public void reset() {

    }

    @Override
    public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {

    }

    @Override
    public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
        if ( ((mCurrentY - pDistanceY) < mMinY)  ){
            return;
        }else if((mCurrentY - pDistanceY) > mMaxY){
            return;
        }

        //Center camera to the current point
        this.mCamera.offsetCenter(0, -pDistanceY);
        mCurrentY -= pDistanceY;

        //Because Camera can have negativ Y values, so set to 0
        if(this.mCamera.getYMin()<0){
            this.mCamera.offsetCenter(0,0);
            mCurrentY =0;
        }
    }

    @Override
    public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {

    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}