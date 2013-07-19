package com.pukku.accelero;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.shape.Shape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.BaseGameActivity;

import android.content.Intent;
import android.content.SharedPreferences;

public class LevelSelector extends BaseGameActivity {

	// ---------------------------------------------
	// CONSTANTS
	// ---------------------------------------------

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------

	private Camera mCamera;
	private SharedPreferences audioOptions;
	private Scene mainScene;

	// ---------------------------------------------
	// TEXTURES & TEXTURE REGIONS
	// ---------------------------------------------

	private BitmapTextureAtlas levelSelectTexture;
	private BitmapTextureAtlas levelsTexture;
	private ITextureRegion levelSelectTextureRegion;
	private ITextureRegion level1TextureRegion;
	private ITextureRegion level2TextureRegion;
	private ITextureRegion level3TextureRegion;

	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------

	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub
		audioOptions = getSharedPreferences("audio", MODE_PRIVATE);
		Camera mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions eng = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
		return eng;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		// TODO Auto-generated method stub

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		levelSelectTexture = new BitmapTextureAtlas(this.getTextureManager(),
				1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		levelSelectTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(levelSelectTexture, this.getAssets(),
						"levelselect.png", 0, 0);
		levelSelectTexture.load();

		levelsTexture = new BitmapTextureAtlas(this.getTextureManager(), 512,
				512);
		level1TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(levelsTexture, this.getAssets(),
						"level1s.png", 0, 0);
		level2TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(levelsTexture, this.getAssets(),
						"level2s.png", 0, 255);
		level3TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(levelsTexture, this.getAssets(),
						"level3s.png", 177, 0);
		levelsTexture.load();
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub

		mainScene = new Scene();
		Sprite back = new Sprite(0, 0, levelSelectTextureRegion,
				this.getVertexBufferObjectManager());
		mainScene.attachChild(back);

		Sprite level1 = new Sprite(55, 109, level1TextureRegion,
				this.getVertexBufferObjectManager()) {

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub

				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					Intent level1Intent = new Intent(LevelSelector.this,
							Level1Activity.class);
					startActivity(level1Intent);
				}
				return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX,
						pTouchAreaLocalY);
			}

		};

		Sprite level2 = new Sprite(269, 109, level2TextureRegion,
				this.getVertexBufferObjectManager()) {

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub

				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					Intent level2Intent = new Intent(LevelSelector.this,
							Level2Activity.class);
					startActivity(level2Intent);
				}
				return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX,
						pTouchAreaLocalY);
			}

		};

		Sprite level3 = new Sprite(487, 109, level3TextureRegion,
				this.getVertexBufferObjectManager()) {

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub

				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					Intent level3Intent = new Intent(LevelSelector.this,
							Level3Activity.class);
					startActivity(level3Intent);
				}
				return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX,
						pTouchAreaLocalY);
			}

		};

		mainScene.registerTouchArea(level1);
		mainScene.registerTouchArea(level2);
		mainScene.registerTouchArea(level3);
		mainScene.setTouchAreaBindingOnActionDownEnabled(true);

		mainScene.getLastChild().attachChild(level1);
		mainScene.getLastChild().attachChild(level2);
		mainScene.getLastChild().attachChild(level3);

		pOnCreateSceneCallback.onCreateSceneFinished(mainScene);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		// TODO Auto-generated method stub

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	public synchronized void onPauseGame() {
		// TODO Auto-generated method stub
		super.onPauseGame();
		if (StartActivity.mMusic.isPlaying()) {
			StartActivity.mMusic.pause();
		}
	}

	@Override
	public synchronized void onResumeGame() {
		// TODO Auto-generated method stub
		super.onResumeGame();
		if (audioOptions.getBoolean("musicOn", true)) {
			StartActivity.mMusic.resume();
		}
	}
}
