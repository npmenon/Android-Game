package com.pukku.accelero;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.BaseGameActivity;

import android.content.SharedPreferences;

public class Help extends BaseGameActivity {

	// ---------------------------------------------
	// CONSTANTS
	// ---------------------------------------------

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------

	private Camera mCamera;

	private Scene helpScene;
	private SharedPreferences audioOptions;

	// ---------------------------------------------
	// TEXTURES & TEXTURE REGIONS
	// ---------------------------------------------
	private BitmapTextureAtlas helpTexture;
	private ITextureRegion helpTextureRegion;

	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------
	
	//Initializing the engine. Engine is the brain of AndEngine and controls and coordinates everything.
	//onCreateEngineOptions initialized the engine.
	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);// Camera(top,left,botton,right)
		audioOptions = getSharedPreferences("audio", MODE_PRIVATE);
		EngineOptions menuOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		return menuOptions;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		// TODO Auto-generated method stub

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		helpTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024,
				512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		helpTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(helpTexture, this.getAssets(), "helpm.png", 0,
						0);
		helpTexture.load();

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub

		this.helpScene = new Scene();
		Sprite helpSprite = new Sprite(0, 0, helpTextureRegion,
				this.getVertexBufferObjectManager());
		helpScene.attachChild(helpSprite);
		pOnCreateSceneCallback.onCreateSceneFinished(helpScene);
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
