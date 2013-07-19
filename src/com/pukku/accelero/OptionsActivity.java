package com.pukku.accelero;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;

import android.content.SharedPreferences;
import android.os.Handler;

public class OptionsActivity extends BaseGameActivity implements
		IOnMenuItemClickListener {

	// ---------------------------------------------
	// CONSTANTS
	// ---------------------------------------------

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	protected static final int MENU_MUSIC = 0;
	protected static final int MENU_EFFECTS = MENU_MUSIC + 1;

	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------

	private Camera mCamera;
	protected Handler mHandler;
	protected Scene mMainScene;
	protected MenuScene mOptionsMenuScene;

	// ---------------------------------------------
	// TEXTURES & TEXTURE REGIONS
	// ---------------------------------------------

	private BitmapTextureAtlas mOptionsBackTexture;
	private ITextureRegion mOptionsBackTextureRegion;

	private SharedPreferences audioOptions;
	private SharedPreferences.Editor audioEditor;

	private TextMenuItem mTurnMusicOff, mTurnMusicOn;
	private TextMenuItem mTurnEffectsOff, mTurnEffectsOn;
	private IMenuItem musicMenuItem;
	private IMenuItem effectsMenuItem;
	private Font mFont;
	private ITexture mFontTexture;

	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------

	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub
		mHandler = new Handler();
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		audioOptions = getSharedPreferences("audio", MODE_PRIVATE);
		audioEditor = audioOptions.edit();
		EngineOptions Options = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		return Options;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		// TODO Auto-generated method stub

		this.mFontTexture = new BitmapTextureAtlas(this.getTextureManager(),
				256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		FontFactory.setAssetBasePath("font/");
		mFont = FontFactory.createFromAsset(this.getFontManager(),
				this.mFontTexture, this.getAssets(), "flubber.TTF", 60.0f,
				true, android.graphics.Color.BLACK);
		mFont.load();

		mTurnMusicOn = new TextMenuItem(MENU_MUSIC, mFont, "Turn Music On",
				this.getVertexBufferObjectManager());
		mTurnMusicOff = new TextMenuItem(MENU_MUSIC, mFont, "Turn Music Off",
				this.getVertexBufferObjectManager());
		mTurnEffectsOn = new TextMenuItem(MENU_EFFECTS, mFont,
				"Turn Effects On", this.getVertexBufferObjectManager());
		mTurnEffectsOff = new TextMenuItem(MENU_EFFECTS, mFont,
				"Turn Effects Off", this.getVertexBufferObjectManager());

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		this.mOptionsBackTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 1024, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mOptionsBackTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mOptionsBackTexture, this, "optionmenu.png",
						0, 0);
		this.mOptionsBackTexture.load();

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.createOptionsMenuScene();

		this.mMainScene = new Scene();
		Sprite optBack = new Sprite(0, 0, this.mOptionsBackTextureRegion,
				this.getVertexBufferObjectManager());
		mMainScene.attachChild(optBack);
		mMainScene.setChildScene(mOptionsMenuScene);
		pOnCreateSceneCallback.onCreateSceneFinished(mMainScene);
	}

	private void createOptionsMenuScene() {
		// TODO Auto-generated method stub

		this.mOptionsMenuScene = new MenuScene(this.mCamera);

		if (audioOptions.getBoolean("musicOn", true)) {

			musicMenuItem = new ColorMenuItemDecorator(mTurnMusicOff,
					new Color(0, 0, 0), new Color(0, 0, 1.0f));
			musicMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
					GL10.GL_ONE_MINUS_SRC_ALPHA);
		} else {

			musicMenuItem = new ColorMenuItemDecorator(mTurnMusicOn, new Color(
					0, 0, 0), new Color(0, 0, 1.0f));
		}
		musicMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mOptionsMenuScene.addMenuItem(musicMenuItem);

		if (audioOptions.getBoolean("effectsOn", true)) {

			effectsMenuItem = new ColorMenuItemDecorator(mTurnEffectsOff,
					new Color(0, 0, 0), new Color(0, 0, 1.0f));
		} else {

			effectsMenuItem = new ColorMenuItemDecorator(mTurnEffectsOn,
					new Color(0, 0, 0), new Color(0, 0, 1.0f));
		}
		effectsMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mOptionsMenuScene.addMenuItem(effectsMenuItem);

		this.mOptionsMenuScene.buildAnimations();
		this.mOptionsMenuScene.setBackgroundEnabled(false);
		this.mOptionsMenuScene.setOnMenuItemClickListener(this);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		// TODO Auto-generated method stub
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		// TODO Auto-generated method stub
		switch (pMenuItem.getID()) {
		case MENU_MUSIC:
			if (audioOptions.getBoolean("musicOn", true)) {
				audioEditor.putBoolean("musicOn", false);
				if (StartActivity.mMusic.isPlaying()) {
					StartActivity.mMusic.pause();
				}
			} else {
				audioEditor.putBoolean("musicOn", true);
				StartActivity.mMusic.resume();
			}
			audioEditor.commit();

			mOptionsMenuScene.clearMenuItems();
			createOptionsMenuScene();
			mMainScene.clearChildScene();
			mMainScene.setChildScene(mOptionsMenuScene);
			return true;

		case MENU_EFFECTS:
			if (audioOptions.getBoolean("effectsOn", true)) {
				audioEditor.putBoolean("effectsOn", false);
			} else {
				audioEditor.putBoolean("effectsOn", true);
			}
			audioEditor.commit();
			mOptionsMenuScene.clearMenuItems();
			createOptionsMenuScene();
			mMainScene.clearChildScene();
			mMainScene.setChildScene(mOptionsMenuScene);
			return true;
		default:
			return false;
		}
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
		mMainScene.registerEntityModifier(new ScaleAtModifier(0.5f, 0.0f, 1.0f,
				CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2));
		mOptionsMenuScene.registerEntityModifier(new ScaleAtModifier(0.5f,
				0.0f, 1.0f, CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2));

	}

}
