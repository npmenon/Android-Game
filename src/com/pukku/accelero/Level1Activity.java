package com.pukku.accelero;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

@SuppressWarnings("unused")
public class Level1Activity extends BaseGameActivity implements
		IAccelerationListener {

	// ===========================================================
	// CONSTANTS
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private static int SPR_COLUMN = 4;
	private static int SPR_ROWS = 2;

	private static int COIN_COLUMN = 4;
	private static int COIN_ROWS = 1;

	private static int POPUP_COLUMNS = 2;
	private static int POP_ROWS = 2;

	private static final int COIN_SCORE = 50;
	private static final int COIN_COLLECT = 1;

	private static final String heroID = "hero";
	private static final String enemyID = "enemy";
	private static final String groundID = "ground";
	private static final String finishID = "gamefinish";
	private static final String coinID = "coin";

	private static final boolean PLAYER_WINS = true;
	private static final String TAG = "GameActivity";

	private static Sound mExplosion, mGameOver, mApplause, mMunchCoin;

	// ===========================================================
	// VARIABLES
	// ===========================================================

	private Camera gameCamera;

	private SharedPreferences scoresOptions;
	private SharedPreferences.Editor scoresEditor;

	private SharedPreferences audioOptions;
	private Handler mHandler;

	private Scene background, playAgainScene, popUp, gameOverScene, scoreScene;
	PhysicsWorld mPhysicsWorld;
	private ArrayList<JSONObject> deletejsonList;

	private JSONObject coinObject, heroObject, enemyObject, groundObject,
			finishObject;
	private MenuScene mTryAgainS;
	private Text coinscoreText, showScoreText;
	private String YourScoreIs, CurrentScore;
	private int coinScoreAdd = 0;
	private int highScores;

	// ---------------------------------------------
	// TEXTURES & TEXTURE REGIONS
	// ---------------------------------------------

	private BitmapTextureAtlas FlubberTexture, enemyTexture, diamondTexture,
			finishTexture;
	private ITextureRegion FlubberTextureRegion, enemyTextureRegion,
			finishTextureRegion, diamondTextureRegion;
	private BitmapTextureAtlas coinTexture;
	private TiledTextureRegion coinTextureRegion;
	private BitmapTextureAtlas BackGroundTexture;
	private ITextureRegion BackGroundTextureRegion, gameOverTextureRegion;
	private BitmapTextureAtlas enemyAnimTexture, popUpTexture, gameOverTexture;
	private TiledTextureRegion enemyAnimTextureRegion, popUpTextureRegion;

	private ITexture mFontTexture;
	private Font mFont;

	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------

	@Override
	public EngineOptions onCreateEngineOptions() {

		deletejsonList = new ArrayList<JSONObject>();
		mHandler = new Handler();

		audioOptions = getSharedPreferences("audio", MODE_PRIVATE);
		scoresOptions = getSharedPreferences("scores", MODE_PRIVATE);
		scoresEditor = scoresOptions.edit();
		highScores = scoresOptions.getInt("Level1", 0);

		scoresEditor.commit();

		gameCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions gameOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.gameCamera);
		gameOptions.getAudioOptions().setNeedsSound(true);
		return gameOptions;

	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		// TODO Auto-generated method stub

		Log.i(TAG, "OnCreateResources");
		this.mFontTexture = new BitmapTextureAtlas(this.getTextureManager(),
				256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		FontFactory.setAssetBasePath("font/");
		mFont = FontFactory.createFromAsset(this.getFontManager(),
				this.mFontTexture, this.getAssets(), "texas.ttf", 72.0f, true,
				Color.GREEN);
		mFont.load();

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		gameOverTexture = new BitmapTextureAtlas(this.getTextureManager(), 512,
				512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameOverTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameOverTexture, this.getAssets(),
						"gameover4.png", 0, 0);
		gameOverTexture.load();

		popUpTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024,
				512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		popUpTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(popUpTexture, this.getAssets(),
						"newwin.png", 0, 0, POPUP_COLUMNS, POP_ROWS);
		popUpTexture.load();

		BackGroundTexture = new BitmapTextureAtlas(this.getTextureManager(),
				128, 128, TextureOptions.REPEATING_BILINEAR_PREMULTIPLYALPHA);
		BackGroundTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(BackGroundTexture, this, "background.png", 0,
						0);
		BackGroundTexture.load();

		finishTexture = new BitmapTextureAtlas(this.getTextureManager(), 64,
				64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		finishTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(finishTexture, this, "finish.png", 0, 0);
		finishTexture.load();

		FlubberTexture = new BitmapTextureAtlas(this.getTextureManager(), 64,
				64);
		FlubberTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(FlubberTexture, this, "player.png", 0, 0);
		FlubberTexture.load();

		coinTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 64,
				TextureOptions.REPEATING_BILINEAR_PREMULTIPLYALPHA);
		coinTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(coinTexture, this.getAssets(),
						"jump_coin.png", 0, 0, COIN_COLUMN, COIN_ROWS);
		coinTexture.load();

		enemyAnimTexture = new BitmapTextureAtlas(this.getTextureManager(),
				256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		enemyAnimTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(enemyAnimTexture, this.getAssets(),
						"enemyss.png", 0, 0, SPR_COLUMN, SPR_ROWS);
		enemyAnimTexture.load();

		SoundFactory.setAssetBasePath("mfx/");
		try {
			Level1Activity.mExplosion = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"explosion.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level1Activity.mGameOver = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"game_over.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level1Activity.mApplause = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"applause.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level1Activity.mMunchCoin = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"munch.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		Log.i(TAG, "OnCreateResources");
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub
		Log.i(TAG, "OnCreateScene");

		background = new Scene();
		background.setBackground(new Background(0, 0, 0));
		
		mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);

		popUp = new Scene();
		popUp.setBackground(new Background(0, 0, 0));

		gameOverScene = new Scene();
		gameOverScene.setBackground(new Background(0, 0, 0));

		scoreScene = new Scene();
		scoreScene.setBackground(new Background(0, 0, 0));

		YourScoreIs = "Your Score is:~ ";
		coinscoreText = new Text(0, 0, mFont, YourScoreIs,
				this.getVertexBufferObjectManager());
		coinscoreText.setPosition(10,
				(CAMERA_HEIGHT - coinscoreText.getHeight()) / 2);
		scoreScene.attachChild(coinscoreText);

		CurrentScore = new String();
		showScoreText = new Text(0, 0, mFont, this.CurrentScore,
				(this.CurrentScore + "XXXX" + "New High Score!!").length(),
				this.getVertexBufferObjectManager());
		showScoreText.setPosition(coinscoreText.getWidth() + 15,
				(CAMERA_HEIGHT - coinscoreText.getHeight()) / 2);
		scoreScene.getLastChild().attachChild(showScoreText);

		BackGroundTextureRegion.setTextureWidth(CAMERA_WIDTH);
		BackGroundTextureRegion.setTextureHeight(CAMERA_HEIGHT);
		Sprite back = new Sprite(0, 0, this.BackGroundTextureRegion,
				this.getVertexBufferObjectManager());
		this.background.attachChild(back);

		Sprite gameOver = new Sprite(0, 0, gameOverTextureRegion,
				this.getVertexBufferObjectManager());
		gameOver.setPosition((CAMERA_WIDTH - gameOver.getWidth()) / 2,
				CAMERA_HEIGHT - gameOver.getHeight() - 80);
		this.gameOverScene.attachChild(gameOver);

		Sprite back1 = new Sprite(0, 0, this.BackGroundTextureRegion,
				this.getVertexBufferObjectManager());
		this.popUp.attachChild(back1);

		createWalls();

		this.background.registerUpdateHandler(mPhysicsWorld);
		Log.i(TAG, "OnCreateScene");
		pOnCreateSceneCallback.onCreateSceneFinished(background);
	}

	private void createWalls() {
		// TODO Auto-generated method stub
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2,
				CAMERA_WIDTH, 2, this.getVertexBufferObjectManager());
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2,
				this.getVertexBufferObjectManager());
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT,
				this.getVertexBufferObjectManager());
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2,
				CAMERA_HEIGHT, this.getVertexBufferObjectManager());
		
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0,
				0.5f, 0.5f);
		
		Body WALL_GROUND = PhysicsFactory.createBoxBody(mPhysicsWorld, ground,
				BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_GROUND, ground);
		WALL_GROUND.setUserData(groundObject);
		Body WALL_ROOF = PhysicsFactory.createBoxBody(mPhysicsWorld, roof,
				BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_ROOF, roof);
		WALL_ROOF.setUserData(groundObject);
		Body WALL_LEFT = PhysicsFactory.createBoxBody(mPhysicsWorld, left,
				BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_LEFT, left);
		WALL_LEFT.setUserData(groundObject);
		Body WALL_RIGHT = PhysicsFactory.createBoxBody(mPhysicsWorld, right,
				BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_RIGHT, right);
		WALL_RIGHT.setUserData(groundObject);

		Entity rectangleGroup = new Entity();
		rectangleGroup.attachChild(ground);
		rectangleGroup.attachChild(roof);
		rectangleGroup.attachChild(left);
		rectangleGroup.attachChild(right);

		rectangleGroup.setColor(0, 0, 0);
		this.background.getLastChild().attachChild(rectangleGroup);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		// TODO Auto-generated method stub
		Log.i(TAG, "OnpopulateScene");

		AnimatedSprite pop = new AnimatedSprite(0, 0, this.popUpTextureRegion,
				this.getVertexBufferObjectManager());
		pop.setPosition((CAMERA_WIDTH - pop.getWidth()) / 2,
				(CAMERA_HEIGHT - pop.getHeight()) / 2);
		this.popUp.getLastChild().attachChild(pop);
		pop.animate(150);

		Sprite finish = new Sprite(0, 0, finishTextureRegion,
				this.getVertexBufferObjectManager());
		finish.setPosition(CAMERA_WIDTH - finish.getWidth(), 8);
		final Body finishBody = PhysicsFactory.createBoxBody(mPhysicsWorld,
				finish, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(finish,
				finishBody, true, false));
		finishObject = makeJsonData(finishID, finishBody, finish);
		finishBody.setUserData(finishObject);
		this.background.getLastChild().attachChild(finish);

		Sprite flubber = new Sprite(0, 0, FlubberTextureRegion,
				this.getVertexBufferObjectManager());
		flubber.setPosition((FlubberTextureRegion.getWidth() + 10),
				(CAMERA_HEIGHT - FlubberTextureRegion.getHeight()) - 10);
		final FixtureDef PLAYER_FIX = PhysicsFactory.createFixtureDef(0.5f,
				0.2f, 0.2f);
		final Body body = PhysicsFactory.createCircleBody(mPhysicsWorld,
				flubber, BodyType.DynamicBody, PLAYER_FIX);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(flubber,
				body, true, false));
		heroObject = makeJsonData(heroID, body, flubber);
		body.setUserData(heroObject);
		// body.setBullet(true);
		this.background.getLastChild().attachChild(flubber);

		AnimatedSprite coin = new AnimatedSprite(0, 0, coinTextureRegion,
				this.getVertexBufferObjectManager());
		coin.setPosition((CAMERA_WIDTH - coin.getWidth()) / 2, 10);
		final Body coinBody = PhysicsFactory.createCircleBody(mPhysicsWorld,
				coin, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coin,
				coinBody, true, false));
		this.background.getLastChild().attachChild(coin);
		coinObject = makeJsonData(coinID, coinBody, coin);
		deletejsonList.add(coinObject);
		coinBody.setUserData(coinObject);
		coin.animate(100);

		AnimatedSprite enemyAnim = new AnimatedSprite(0, 0,
				enemyAnimTextureRegion, this.getVertexBufferObjectManager());
		final Body enemyAnimBod = PhysicsFactory.createCircleBody(
				mPhysicsWorld, enemyAnim, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemyAnim,
				enemyAnimBod, true, false));
		enemyAnimBod.setUserData(enemyID); // Error here...setuserdata as json
											// object
		this.background.getLastChild().attachChild(enemyAnim);
		enemyAnim.animate(100);

		AnimatedSprite coin2 = new AnimatedSprite(0, 0, coinTextureRegion,
				this.getVertexBufferObjectManager());
		coin2.setPosition(enemyAnim.getWidth() + 2, 2);
		final Body coinBody2 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				coin2, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coin2,
				coinBody2, true, false));
		this.background.getLastChild().attachChild(coin2);
		coinObject = makeJsonData(coinID, coinBody2, coin2);
		deletejsonList.add(coinObject);
		coinBody2.setUserData(coinObject);
		coin2.animate(100);

		AnimatedSprite enemySaw = new AnimatedSprite(0, 0,
				enemyAnimTextureRegion, this.getVertexBufferObjectManager());
		enemySaw.setPosition(enemySaw.getWidth() + 5, enemySaw.getHeight() + 15);
		final Body enemyBody1 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				enemySaw, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		enemyObject = makeJsonData(enemyID, enemyBody1, enemySaw);
		enemyBody1.setUserData(enemyObject);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemySaw,
				enemyBody1, true, false));
		this.background.getLastChild().attachChild(enemySaw);
		enemySaw.animate(100);

		AnimatedSprite coin3 = new AnimatedSprite(0, 0, coinTextureRegion,
				this.getVertexBufferObjectManager());
		coin3.setPosition(2 * (enemySaw.getWidth()) + 5,
				2 * (enemySaw.getHeight()) + 10);
		final Body coinBody3 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				coin3, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coin3,
				coinBody3, true, false));
		this.background.getLastChild().attachChild(coin3);
		coinObject = makeJsonData(coinID, coinBody3, coin3);
		deletejsonList.add(coinObject);
		coinBody3.setUserData(coinObject);
		coin3.animate(100);

		AnimatedSprite enemySaw2 = new AnimatedSprite(0, 0,
				enemyAnimTextureRegion, this.getVertexBufferObjectManager());
		enemySaw2.setPosition(enemySaw.getX() + enemySaw2.getWidth() + 80,
				2 * (enemySaw2.getHeight()) + 50);
		final Body enemyBody2 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				enemySaw2, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		enemyObject = makeJsonData(enemyID, enemyBody2, enemySaw2);
		enemyBody2.setUserData(enemyObject);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemySaw2,
				enemyBody2, true, false));
		this.background.getLastChild().attachChild(enemySaw2);
		enemySaw2.animate(100);

		AnimatedSprite coin4 = new AnimatedSprite(0, 0, coinTextureRegion,
				this.getVertexBufferObjectManager());
		coin4.setPosition(
				enemySaw.getX() + enemySaw2.getWidth() + 80 + coin4.getWidth(),
				2 * (enemySaw2.getHeight()) + 50 + coin4.getHeight());
		final Body coinBody4 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				coin4, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coin4,
				coinBody4, true, false));
		this.background.getLastChild().attachChild(coin4);
		coinObject = makeJsonData(coinID, coinBody4, coin4);
		deletejsonList.add(coinObject);
		coinBody4.setUserData(coinObject);
		coin4.animate(100);

		AnimatedSprite enemySaw3 = new AnimatedSprite(0, 0,
				enemyAnimTextureRegion, this.getVertexBufferObjectManager());
		enemySaw3.setPosition((CAMERA_WIDTH - enemySaw3.getWidth() - 25),
				(CAMERA_HEIGHT - enemySaw3.getWidth() - 15));
		final Body enemyBody3 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				enemySaw3, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		enemyObject = makeJsonData(enemyID, enemyBody3, enemySaw3);
		enemyBody3.setUserData(enemyObject);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemySaw3,
				enemyBody3, true, false));
		this.background.getLastChild().attachChild(enemySaw3);
		enemySaw3.animate(100);

		AnimatedSprite coin5 = new AnimatedSprite(0, 0, coinTextureRegion,
				this.getVertexBufferObjectManager());
		coin5.setPosition(
				(CAMERA_WIDTH - enemySaw3.getWidth() - 25 - coin5.getWidth()),
				(CAMERA_HEIGHT - enemySaw3.getWidth() - 15 - coin5.getHeight()));
		final Body coinBody5 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				coin5, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coin5,
				coinBody5, true, false));
		this.background.getLastChild().attachChild(coin5);
		coinObject = makeJsonData(coinID, coinBody5, coin5);
		deletejsonList.add(coinObject);
		coinBody5.setUserData(coinObject);
		coin5.animate(100);

		AnimatedSprite enemySaw4 = new AnimatedSprite(0, 0,
				enemyAnimTextureRegion, this.getVertexBufferObjectManager());
		enemySaw4.setPosition(
				1.4f * ((CAMERA_WIDTH - enemySaw2.getWidth()) / 2) + 30,
				1.4f * ((CAMERA_HEIGHT - enemySaw2.getWidth()) / 2) + 20);
		final Body enemyBody4 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				enemySaw4, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		enemyObject = makeJsonData(enemyID, enemyBody4, enemySaw4);
		enemyBody4.setUserData(enemyObject);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemySaw4,
				enemyBody4, true, false));
		this.background.getLastChild().attachChild(enemySaw4);
		enemySaw4.animate(100);

		AnimatedSprite enemySaw5 = new AnimatedSprite(0, 0,
				enemyAnimTextureRegion, this.getVertexBufferObjectManager());
		enemySaw5.setPosition((CAMERA_WIDTH / 2), (CAMERA_HEIGHT) / 2);
		final Body enemyBody5 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				enemySaw5, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		enemyObject = makeJsonData(enemyID, enemyBody5, enemySaw5);
		enemyBody5.setUserData(enemyObject);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemySaw5,
				enemyBody5, true, false));
		this.background.getLastChild().attachChild(enemySaw5);
		enemySaw5.animate(100);

		AnimatedSprite coin6 = new AnimatedSprite(0, 0, coinTextureRegion,
				this.getVertexBufferObjectManager());
		coin6.setPosition((CAMERA_WIDTH / 2) + coin6.getWidth(),
				(CAMERA_HEIGHT) / 2 + coin6.getHeight());
		final Body coinBody6 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				coin6, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coin6,
				coinBody6, true, false));
		this.background.getLastChild().attachChild(coin6);
		coinObject = makeJsonData(coinID, coinBody6, coin6);
		deletejsonList.add(coinObject);
		coinBody6.setUserData(coinObject);
		coin6.animate(100);

		AnimatedSprite enemySaw6 = new AnimatedSprite(0, 0,
				enemyAnimTextureRegion, this.getVertexBufferObjectManager());
		enemySaw6.setPosition(CAMERA_WIDTH - finish.getWidth() - 80,
				finish.getHeight() + 10);
		final Body enemyBody6 = PhysicsFactory.createCircleBody(mPhysicsWorld,
				enemySaw6, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		enemyObject = makeJsonData(enemyID, enemyBody6, enemySaw6);
		enemyBody6.setUserData(enemyObject);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemySaw6,
				enemyBody6, true, false));
		this.background.getLastChild().attachChild(enemySaw6);
		enemySaw6.animate(100);

		this.mPhysicsWorld.setContinuousPhysics(true);

		this.mPhysicsWorld.setContactListener(new ContactListener() {

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub

			}

			@Override
			public void endContact(Contact contact) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beginContact(Contact contact) {
				// TODO Auto-generated method stub

				Body bodyA = contact.getFixtureA().getBody();
				Body bodyB = contact.getFixtureB().getBody();

				JSONObject obA = (JSONObject) bodyA.getUserData();
				JSONObject obB = (JSONObject) bodyB.getUserData();

				try {
					if ((obA.get("ID").equals(heroID) && obB.get("ID").equals(
							enemyID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(enemyID))) {

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(enemyID)) {

							bodyA.setType(BodyType.StaticBody);

						} else {

							bodyB.setType(BodyType.StaticBody);

						}

						if (audioOptions.getBoolean("effectsOn", true)) {

							Level1Activity.mExplosion.play();
							mHandler.postDelayed(mPlayGameOverSound, 2000);
							mHandler.postDelayed(mTry, 3500);
						}

						else {
							mEngine.setScene(gameOverScene);
							mHandler.postDelayed(mTry, 1500);
						}

					}

					if ((obA.get("ID").equals(heroID) && obB.get("ID").equals(
							finishID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(finishID))) {

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(finishID)) {

							bodyA.setType(BodyType.StaticBody);

						} else if (obB.get("ID").equals(heroID)
								&& obA.get("ID").equals(finishID)) {

							bodyB.setType(BodyType.StaticBody);

						}

						Log.i(TAG, "Entered finish method");

						final String scorenow;
						scorenow = Integer.toString(scoresOptions.getInt(
								"score", 0));

						if (highScores < scoresOptions.getInt("score", 0)) {
							scoresEditor.putInt("Level1", highScores);
							scoresEditor.commit();
						}
						showScoreText.setText(scorenow);
						// If current score is greater than high score

						Log.i(TAG,
								"Entered finish method-checking sound on or off");
						if (audioOptions.getBoolean("effectsOn", true)) {

							Level1Activity.mApplause.play();
							// background.clearChildScene();
							Log.i(TAG, "setting popup scene");
							mEngine.setScene(popUp);
							mHandler.postDelayed(mCoinSceneAdd, 3500);
							mHandler.postDelayed(mEndGame, 6500);
						} else {
							mEngine.setScene(popUp);
							mHandler.postAtTime(mCoinSceneAdd, 3500);
							mHandler.postAtTime(mEndGame, 6500);
						}

						Log.i(TAG, "Entered finish method");

					} else if ((obA.get("ID").equals(heroID) && obB.get("ID")
							.equals(coinID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(coinID))) {

						Log.i(TAG,
								"entered contact method between coin and hero");

						coinScoreAdd += COIN_SCORE;
						scoresEditor.putInt("score", coinScoreAdd);
						scoresEditor.commit();

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(coinID)) {

							obB.put("deleteStatus", true);

						} else if (obB.get("ID").equals(heroID)
								&& obA.get("ID").equals(coinID)) {

							obA.put("deleteStatus", true);

						}

						if (audioOptions.getBoolean("effectsOn", true)) {

							Level1Activity.mMunchCoin.play();

						}

					}

				} catch (JSONException e) {
					// TODO: handle exception
					Log.e(TAG, "error at contact listener");
				}

			}

		});

		background.registerUpdateHandler(this.removeCoinUpdatehandler());
		Log.i(TAG, "OnpopulateScene");
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	private JSONObject makeJsonData(String identify, Body body, Object sprite) {

		JSONObject myObject = new JSONObject();

		try {
			myObject.put("ID", identify);
			myObject.put("deleteStatus", false);
			myObject.put("body", body);
			myObject.put("sprite", sprite);

		} catch (JSONException e) {
			// TODO: handle exception
			Log.d(TAG, "Exception creating user data:" + e);
		}
		return myObject;
	}

	private IUpdateHandler removeCoinUpdatehandler() {

		return new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub

				JSONObject currentJsonObj;

				for (int i = 0; i < Level1Activity.this.deletejsonList.size(); i++) {

					currentJsonObj = Level1Activity.this.deletejsonList.get(i);

					try {

						if (currentJsonObj.getBoolean("deleteStatus")) {
							Log.i(TAG, "Entered method to remove coin");

							currentJsonObj.put("deleteStatus", false);
							
							final AnimatedSprite Object = (AnimatedSprite) currentJsonObj
									.get("sprite");
							final Body body = (Body) currentJsonObj.get("body");
							mPhysicsWorld.destroyBody(body);
							Object.setVisible(false);
							Log.i(TAG, "Entered method to remove coin");

						}
					} catch (JSONException e) {
						// TODO: handle exception
						Log.i(TAG, "exception inside");
					}
				}
			}
		};
	}

	private Runnable mCoinSceneAdd = new Runnable() {
		public void run() {

			mEngine.setScene(scoreScene);
		}
	};

	private Runnable mPlayGameOverSound = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mEngine.setScene(gameOverScene);
			Level1Activity.mGameOver.play();
		}
	};
	private Runnable mEndGame = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Level1Activity.this.finish();
			Intent maintent = new Intent(Level1Activity.this,
					MenuActivity.class);
			startActivity(maintent);
		}
	};

	private Runnable mTry = new Runnable() {
		public void run() {

			// mEngine.getScene().back();
			Intent tryIntent = new Intent(Level1Activity.this, TryAgain.class);
			startActivity(tryIntent);
		}
	};

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub

		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(),
				pAccelerationData.getY());
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	public synchronized void onResumeGame() {
		// TODO Auto-generated method stub
		super.onResumeGame();
		this.enableAccelerationSensor(this);
	}

	@Override
	public synchronized void onPauseGame() {
		// TODO Auto-generated method stub
		super.onPauseGame();
		this.disableAccelerationSensor();
	}

	// The onAccelerationAccuracyChanged() and onAccelerationChanged()
	// methods are inherited from the IAccelerationListener interface and allow
	// us to change the gravity of our physics world when the device is tilted,
	// rotated, or
	// panned We override onResumeGame() and onPauseGame() to keep the
	// accelerometer from using unnecessary
	// battery power when our game activity is not in the foreground.

}
