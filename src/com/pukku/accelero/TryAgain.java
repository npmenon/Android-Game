package com.pukku.accelero;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
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

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class TryAgain extends BaseGameActivity implements
		IOnMenuItemClickListener {

	// ---------------------------------------------
	// CONSTANTS
	// ---------------------------------------------

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	protected static final int PLAYAGAIN_YES = 0;
	protected static final int PLAYAGAIN_NO = PLAYAGAIN_YES + 1;

	private static final String TAG = "GameActivity";

	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------

	private Camera mCamera;

	private MenuScene mTryAgainS;
	private Scene gameOverScene2;

	private TextMenuItem yesMenuItem, noMenuItem;
	private IMenuItem yesIMenuItem, noIMenuItem;

	// ---------------------------------------------
	// TEXTURES & TEXTURE REGIONS
	// ---------------------------------------------

	private BitmapTextureAtlas gameOverTryATexture;
	private ITextureRegion gameOverTryATextureRegion;

	private ITexture mFontTexture;
	private Font mFont;

	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------

	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub

		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions tryAgainOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		return tryAgainOptions;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		// TODO Auto-generated method stub

		this.mFontTexture = new BitmapTextureAtlas(this.getTextureManager(),
				256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		FontFactory.setAssetBasePath("font/");
		this.mFont = FontFactory.createFromAsset(this.getFontManager(),
				this.mFontTexture, this.getAssets(), "texas.ttf", 72.0f, true,
				android.graphics.Color.GREEN);
		this.mFont.load();

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		gameOverTryATexture = new BitmapTextureAtlas(this.getTextureManager(),
				512, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameOverTryATextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameOverTryATexture, this.getAssets(),
						"tryagain.png", 0, 0);
		gameOverTryATexture.load();

		yesMenuItem = new TextMenuItem(PLAYAGAIN_YES, this.mFont, "YES",
				this.getVertexBufferObjectManager());
		noMenuItem = new TextMenuItem(PLAYAGAIN_NO, this.mFont, "NO",
				this.getVertexBufferObjectManager());

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub
		this.mEngine.registerUpdateHandler(new FPSLogger());

		Log.i(TAG, "calling createTryagainscene");
		this.createTryAgainScene();

		gameOverScene2 = new Scene();
		gameOverScene2.setBackground(new Background(Color.BLACK));

		Sprite gameOverTryAgain = new Sprite(0, 0, gameOverTryATextureRegion,
				this.getVertexBufferObjectManager());
		gameOverTryAgain.setPosition(
				(CAMERA_WIDTH - gameOverTryAgain.getWidth()) / 2,
				gameOverTryAgain.getHeight() + 10);
		this.gameOverScene2.attachChild(gameOverTryAgain);

		Log.i(TAG, "attached gameOvertryAgain as the ");
		this.gameOverScene2.setChildScene(mTryAgainS);
		Log.i(TAG, "attached mTryAgainS as the child Scene");

		pOnCreateSceneCallback.onCreateSceneFinished(gameOverScene2);
	}

	private void createTryAgainScene() {
		// TODO Auto-generated method stub

		Log.i(TAG, "Entered createTryagainscene");
		this.mTryAgainS = new MenuScene(this.mCamera);

		yesIMenuItem = new ScaleMenuItemDecorator(yesMenuItem, 2.0f, 1.0f);
		yesIMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		yesIMenuItem.setPosition((CAMERA_WIDTH - yesIMenuItem.getWidth()) / 2,
				290);
		this.mTryAgainS.addMenuItem(yesIMenuItem);

		noIMenuItem = new ScaleMenuItemDecorator(noMenuItem, 2.0f, 1.0f);
		noIMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		noIMenuItem.setPosition((CAMERA_WIDTH - noIMenuItem.getWidth()) / 2,
				yesIMenuItem.getY() + 85);
		this.mTryAgainS.addMenuItem(noIMenuItem);

		this.mTryAgainS.setBackgroundEnabled(false);
		this.mTryAgainS.setOnMenuItemClickListener(this);
		Log.i(TAG, "finished createTryagainscene");
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

		case PLAYAGAIN_YES:
			Intent in = new Intent(TryAgain.this, LevelSelector.class);
			startActivity(in);
			return true;

		case PLAYAGAIN_NO:
			// mHandler.postDelayed(mEndGame, 1000);
			Intent i = new Intent(TryAgain.this, MenuActivity.class);
			startActivity(i);
			return true;

		default:
			return false;
		}
	}

}
