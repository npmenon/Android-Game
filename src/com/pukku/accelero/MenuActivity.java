package com.pukku.accelero;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.view.KeyEvent;

@SuppressWarnings("unused")
public class MenuActivity extends BaseGameActivity implements
		IOnMenuItemClickListener {

	// ---------------------------------------------
	// CONSTANTS
	// ---------------------------------------------

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	protected static final int MENU_ABOUT = 0;
	protected static final int MENU_CONTACTUS = MENU_ABOUT + 1;
	protected static final int MENU_PLAY = 100;
	protected static final int MENU_SCORES = MENU_PLAY + 1;
	protected static final int MENU_OPTIONS = MENU_SCORES + 1;
	protected static final int MENU_HELP = MENU_OPTIONS + 1;

	private static int SPR_COLUMN = 4;
	private static int SPR_ROWS = 2;

	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------

	private Camera menuCamera;
	private Handler mHandler;

	private SharedPreferences audioOptions;
	private SharedPreferences.Editor audioEditor;

	protected Scene mainMenuScene;
	protected MenuScene mStaticMenuScene, mPopUpMenuScene;
	StartActivity sa;

	// ---------------------------------------------
	// TEXTURES & TEXTURE REGIONS
	// ---------------------------------------------

	private BitmapTextureAtlas mMenuTexture;
	private ITextureRegion mMenuTextureRegion;
	private BitmapTextureAtlas texBanana;
	private TiledTextureRegion regBanana;
	private AnimatedSprite sprBanana;

	protected ITextureRegion mMenuPlayTextureRegion;
	protected ITextureRegion mMenuScoresTextureRegion;
	protected ITextureRegion mMenuOptionsTextureRegion;
	protected ITextureRegion mMenuHelpTextureRegion;

	private ITexture mFontTexture;
	private Font mFont;

	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------

	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub

		mHandler = new Handler();
		menuCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);// Camera(top,left,bottom,right)
		audioOptions = getSharedPreferences("audio", MODE_PRIVATE);
		audioEditor = audioOptions.edit();
		EngineOptions menuOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.menuCamera);
		return menuOptions;
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
				this.mFontTexture, this.getAssets(), "texas.ttf", 60.0f, true,
				Color.BLACK);
		mFont.load();

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		texBanana = new BitmapTextureAtlas(this.getTextureManager(), 256, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regBanana = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(texBanana, this.getAssets(),
						"spr_banana.png", 0, 0, SPR_COLUMN, SPR_ROWS);
		texBanana.load();

		mMenuTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024,
				512, TextureOptions.BILINEAR);
		mMenuTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mMenuTexture, this.getAssets(), "menu.png", 0,
						0);
		mMenuTexture.load();

		pOnCreateResourcesCallback.onCreateResourcesFinished();

	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub

		this.mEngine.registerUpdateHandler(new FPSLogger());

		// createPopUpMenuScene();
		createStaticMenuScene();
		sprBanana = new AnimatedSprite(0, 0, regBanana,
				this.getVertexBufferObjectManager());
		sprBanana.setPosition(140, 120);

		this.mainMenuScene = new Scene();
		this.mainMenuScene.setBackground(new Background(0, 0, 0));

		Sprite menuBack = new Sprite(0, 0, this.mMenuTextureRegion,
				this.getVertexBufferObjectManager());

		this.mainMenuScene.attachChild(menuBack);
		this.mainMenuScene.getLastChild().attachChild(sprBanana);
		sprBanana.animate(100);
		this.mainMenuScene.setChildScene(mStaticMenuScene);
		pOnCreateSceneCallback.onCreateSceneFinished(mainMenuScene);
	}

	protected void createStaticMenuScene() {
		// TODO Auto-generated method stub

		this.mStaticMenuScene = new MenuScene(this.menuCamera);
		final IMenuItem playMenuItem = new ScaleMenuItemDecorator(
				new TextMenuItem(MENU_PLAY, this.mFont, "Play Game",
						this.getVertexBufferObjectManager()), 2.0f, 1.0f);
		playMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mStaticMenuScene.addMenuItem(playMenuItem);
		
		final IMenuItem optionsMenuItem = new ScaleMenuItemDecorator(
				new TextMenuItem(MENU_OPTIONS, this.mFont, "Options",
						this.getVertexBufferObjectManager()), 2.0f, 1.0f);
		optionsMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mStaticMenuScene.addMenuItem(optionsMenuItem);

		final IMenuItem helpMenuItem = new ScaleMenuItemDecorator(
				new TextMenuItem(MENU_HELP, this.mFont, "Help",
						this.getVertexBufferObjectManager()), 2.0f, 1.0f);
		helpMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mStaticMenuScene.addMenuItem(helpMenuItem);

		this.mStaticMenuScene.buildAnimations();
		this.mStaticMenuScene.setBackgroundEnabled(false);
		this.mStaticMenuScene.setOnMenuItemClickListener(this);
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

		case MENU_PLAY:
			mainMenuScene.registerEntityModifier(new ScaleModifier(1.0f, 1.0f,
					0.0f));
			mStaticMenuScene.registerEntityModifier(new ScaleModifier(1.0f,
					1.0f, 0.0f));
			mHandler.postDelayed(mLaunchLevel1Task, 1000);
			return true;

		case MENU_OPTIONS:
			mainMenuScene.registerEntityModifier(new ScaleModifier(1.0f, 1.0f,
					0.0f));
			mStaticMenuScene.registerEntityModifier(new ScaleModifier(1.0f,
					1.0f, 0.0f));
			mHandler.postDelayed(mLaunchOptionsTask, 1000);
			return true;

		case MENU_HELP:

			mHandler.postDelayed(mLaunchHelp, 1000);
			return true;

		default:
			return false;
		}
	}

	private Runnable mLaunchLevel1Task = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent myIntent = new Intent(MenuActivity.this, LevelSelector.class);
			startActivity(myIntent);
		}
	};

	private Runnable mLaunchOptionsTask = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent myIntent = new Intent(MenuActivity.this,
					OptionsActivity.class);
			startActivity(myIntent);
		}
	};

	private Runnable mLaunchHelp = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent helpIntent = new Intent(MenuActivity.this, Help.class);
			startActivity(helpIntent);
		}
	};

	@Override
	public synchronized void onResumeGame() {
		// TODO Auto-generated method stub

		// sa.finish();
		super.onResumeGame();
		mainMenuScene.registerEntityModifier(new ScaleAtModifier(0.5f, 0.0f,
				1.0f, CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2));
		mStaticMenuScene.registerEntityModifier(new ScaleAtModifier(0.5f, 0.0f,
				1.0f, CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2));

		if (audioOptions.getBoolean("musicOn", true)) {
			StartActivity.mMusic.resume();
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

}
