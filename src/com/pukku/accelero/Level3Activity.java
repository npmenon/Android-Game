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
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

public class Level3Activity extends BaseGameActivity implements
		IAccelerationListener {

	// ---------------------------------------------
	// CONSTANTS
	// ---------------------------------------------

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private static int COIN_COLUMN = 4;
	private static int COIN_ROWS = 2;

	private static final String groundID = "ground";
	private static final String heroID = "hero";
	private static final String knifeID = "knife";
	private static final String rollerID = "roller";
	private static final String finishID = "finish";
	private static final String coinID = "coin";
	private static final String enemyID = "enemy";

	private static final int startingY = 40;
	private static final int endingY = CAMERA_HEIGHT - 128;
	private static final int startingUpY = 352;
	private static final int endingUpY = 40;
	private static final int startingLRY = 10;
	private static final int endingLRY = 590;

	private static final int COIN_SCORE = 50;

	private static int SPR_COLUMN = 4;
	private static int SPR_ROWS = 2;

	private static final String TAG = "GameActivity";

	static private Sound mGameOver, mGrenade, mStab, mApplause, mMunch,
			mExplosion;

	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------

	private ArrayList<JSONObject> deletejsonList;
	private JSONObject groundObject, heroObject, knifeObject, rollerObject,
	finishObject, coinObject, enemyObject;

	private Camera gameCamera;
	private Handler mHandler;
	private Scene gameBackground, gameOverScene, popUp;
	private int direcDown;
	private int direcUp;
	private int direcLR;
	private int direcLR2;

	private Sprite knife, upknife, knifelr, knifelr2;
	private Body knifeDownbody, knifeUpbody, knifelrBody, knifelrBody2;

	private SharedPreferences scoresOptions;
	private SharedPreferences.Editor scoresEditor;
	private SharedPreferences audioOptions;

	private Text coinscoreText, showScoreText;
	private String YourScoreIs, CurrentScore;
	private int coinScoreAdd = 0;
	private int highScores;
	private Scene scoreScene;

	PhysicsWorld level3PhysicsWorld;

	// ---------------------------------------------
	// TEXTURES & TEXTURE REGIONS
	// ---------------------------------------------

	private BitmapTextureAtlas FlubberTexture, enemyTexture, diamondTexture,
			finishTexture, pearlTexture, rollerTexture, coinTexture,
			mBackGroundTexture, knifeUDTexture, knifeLRTexture;
	private ITextureRegion FlubberTextureRegion, enemyTextureRegion,
			finishTextureRegion, diamondTextureRegion, gameOverTextureRegion,
			pearlTextureRegion, mBackGroundTextureRegion, knifeUDTextureRegion,
			knifeLRTextureRegion;
	private BitmapTextureAtlas enemyAnimTexture, popUpTexture, gameOverTexture;
	private TiledTextureRegion enemyAnimTextureRegion, popUpTextureRegion,
			coinTextureRegion, rollerTextureRegion;

	private ITexture mFontTexture;
	private Font mFont;

	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------

	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub

		deletejsonList = new ArrayList<JSONObject>();

		scoresOptions = getSharedPreferences("scores", MODE_PRIVATE);
		scoresEditor = scoresOptions.edit();
		highScores = scoresOptions.getInt("Level3", 0);
		scoresEditor.commit();

		audioOptions = getSharedPreferences("audio", MODE_PRIVATE);
		mHandler = new Handler();
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

		popUpTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024,
				512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		popUpTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(popUpTexture, this.getAssets(),
						"newwin.png", 0, 0, 2, 2);
		popUpTexture.load();

		mBackGroundTexture = new BitmapTextureAtlas(this.getTextureManager(),
				1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mBackGroundTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mBackGroundTexture, this.getAssets(),
						"back3.png", 0, 0);
		mBackGroundTexture.load();

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

		enemyAnimTexture = new BitmapTextureAtlas(this.getTextureManager(),
				256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		enemyAnimTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(enemyAnimTexture, this.getAssets(),
						"enemyss.png", 0, 0, SPR_COLUMN, SPR_ROWS);
		enemyAnimTexture.load();

		gameOverTexture = new BitmapTextureAtlas(this.getTextureManager(), 512,
				512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameOverTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameOverTexture, this.getAssets(),
						"gameover4.png", 0, 0);
		gameOverTexture.load();

		pearlTexture = new BitmapTextureAtlas(this.getTextureManager(), 64, 64,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		pearlTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(pearlTexture, this.getAssets(), "pearl.png",
						0, 0);
		pearlTexture.load();

		rollerTexture = new BitmapTextureAtlas(this.getTextureManager(), 128,
				128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		rollerTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(rollerTexture, this.getAssets(),
						"roller.png", 0, 0, 2, 1);
		rollerTexture.load();

		coinTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 64,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		coinTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(coinTexture, this.getAssets(),
						"coinrotate.png", 0, 0, COIN_COLUMN, COIN_ROWS);
		coinTexture.load();

		Log.i(TAG, "Loading knifedown image");
		knifeUDTexture = new BitmapTextureAtlas(this.getTextureManager(), 32,
				128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		knifeUDTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(knifeUDTexture, this.getAssets(),
						"knifeud.png", 0, 0);
		knifeUDTexture.load();
		Log.i(TAG, "Loading knifedown image");

		Log.i(TAG, "Loading knifeleftright image");
		knifeLRTexture = new BitmapTextureAtlas(this.getTextureManager(), 128,
				32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		knifeLRTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(knifeLRTexture, this.getAssets(),
						"knifelr.png", 0, 0);
		knifeLRTexture.load();
		Log.i(TAG, "Loading knifeleftright image");

		SoundFactory.setAssetBasePath("mfx/");

		try {
			Level3Activity.mExplosion = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"explosion.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level3Activity.mGameOver = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"game_over.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level3Activity.mGrenade = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"grenade.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level3Activity.mStab = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"stab.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level3Activity.mApplause = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), getApplicationContext(),
					"applause.ogg");
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try {
			Level3Activity.mMunch = SoundFactory.createSoundFromAsset(
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

		gameBackground = new Scene();
		gameBackground.setBackground(new Background(0, 0, 0));

		gameOverScene = new Scene();
		gameOverScene.setBackground(new Background(0, 0, 0));

		popUp = new Scene();
		popUp.setBackground(new Background(0, 0, 0));

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

		level3PhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		createWalls();

		Sprite background = new Sprite(0, 0, mBackGroundTextureRegion,
				this.getVertexBufferObjectManager());
		this.gameBackground.attachChild(background);

		Sprite gameOver = new Sprite(0, 0, gameOverTextureRegion,
				this.getVertexBufferObjectManager());
		gameOver.setPosition((CAMERA_WIDTH - gameOver.getWidth()) / 2,
				CAMERA_HEIGHT - gameOver.getHeight() - 80);
		this.gameOverScene.attachChild(gameOver);

		Sprite background1 = new Sprite(0, 0, mBackGroundTextureRegion,
				this.getVertexBufferObjectManager());
		this.popUp.attachChild(background1);

		this.gameBackground.registerUpdateHandler(level3PhysicsWorld);
		pOnCreateSceneCallback.onCreateSceneFinished(gameBackground);
		Log.i(TAG, "OnCreateScene");
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

		Body WALL_GROUND = PhysicsFactory.createBoxBody(level3PhysicsWorld,
				ground, BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_GROUND, ground);
		WALL_GROUND.setUserData(groundObject);

		Body WALL_ROOF = PhysicsFactory.createBoxBody(level3PhysicsWorld, roof,
				BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_ROOF, roof);
		WALL_ROOF.setUserData(groundObject);

		Body WALL_LEFT = PhysicsFactory.createBoxBody(level3PhysicsWorld, left,
				BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_LEFT, left);
		WALL_LEFT.setUserData(groundObject);

		Body WALL_RIGHT = PhysicsFactory.createBoxBody(level3PhysicsWorld,
				right, BodyType.StaticBody, wallFixtureDef);
		groundObject = makeJsonData(groundID, WALL_RIGHT, right);
		WALL_RIGHT.setUserData(groundObject);

		Entity rectangleGroup = new Entity();
		rectangleGroup.attachChild(ground);
		rectangleGroup.attachChild(roof);
		rectangleGroup.attachChild(left);
		rectangleGroup.attachChild(right);

		rectangleGroup.setAlpha(0.0f);
		this.gameBackground.attachChild(rectangleGroup);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		// TODO Auto-generated method stub

		AnimatedSprite pop = new AnimatedSprite(0, 0, this.popUpTextureRegion,
				this.getVertexBufferObjectManager());
		pop.setPosition((CAMERA_WIDTH - pop.getWidth()) / 2,
				(CAMERA_HEIGHT - pop.getHeight()) / 2);
		this.popUp.getLastChild().attachChild(pop);
		pop.animate(150);

		Sprite flubber = new Sprite(0, 0, FlubberTextureRegion,
				this.getVertexBufferObjectManager());
		flubber.setPosition(190, 80);
		final FixtureDef fixDe = PhysicsFactory.createFixtureDef(0.7f, 0.2f,
				0.4f);
		final Body flubberBody = PhysicsFactory.createCircleBody(
				level3PhysicsWorld, flubber, BodyType.DynamicBody, fixDe);
		level3PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				flubber, flubberBody, true, false));
		heroObject = makeJsonData(heroID, flubberBody, flubber);
		flubberBody.setUserData(heroObject);
		this.gameBackground.getLastChild().attachChild(flubber);

		Sprite finish = new Sprite(0, 0, finishTextureRegion,
				this.getVertexBufferObjectManager());
		finish.setPosition(CAMERA_WIDTH - finish.getWidth(), 8);
		final Body finishBody = PhysicsFactory.createBoxBody(
				level3PhysicsWorld, finish, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		level3PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				finish, finishBody, true, false));
		finishObject = makeJsonData(finishID, finishBody, finish);
		finishBody.setUserData(finishObject);
		this.gameBackground.getLastChild().attachChild(finish);

		addCoin(10, 10);
		addCoin(10, 444);
		addCoin(680, 444);

		addRoller(100, 176);
		addCoin(167, 176);
		addCoin(132, 330);

		addRoller(390, 176);
		addCoin(353, 176);
		addCoin(422, 330);

		addRoller(240, 320);
		addCoin(240, 280);

		addCoin(344, 40);

		addEnemySaw(540, 360);
		addEnemySaw(540, 40);
		addEnemySaw(656, 224);

		FixtureDef fixDef = PhysicsFactory.createFixtureDef(0, 0, 0);

		knife = new Sprite(0, 0, knifeUDTextureRegion,
				this.getVertexBufferObjectManager());
		knife.setPosition(CAMERA_WIDTH - 40, 44);
		knifeDownbody = PhysicsFactory.createBoxBody(level3PhysicsWorld, knife,
				BodyType.KinematicBody, fixDef);
		knifeObject = makeJsonData(knifeID, knifeDownbody, knife);
		knifeDownbody.setUserData(knifeObject);
		knifeDownbody.setLinearVelocity(0, 70);
		// knifeDownbody.setBullet(true);
		direcDown = 1;
		level3PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(knife,
				knifeDownbody, true, false));
		this.gameBackground.registerUpdateHandler(this.setDownVelocity());
		this.gameBackground.getLastChild().attachChild(knife);

		upknife = new Sprite(0, 0, knifeUDTextureRegion,
				this.getVertexBufferObjectManager());
		upknife.setPosition(5, 340);
		knifeUpbody = PhysicsFactory.createBoxBody(level3PhysicsWorld, upknife,
				BodyType.KinematicBody, fixDef);
		knifeObject = makeJsonData(knifeID, knifeUpbody, upknife);
		knifeUpbody.setUserData(knifeObject);
		knifeUpbody.setLinearVelocity(0, -70);
		direcUp = 2;
		level3PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				upknife, knifeUpbody, true, false));
		this.gameBackground.registerUpdateHandler(this.setUpVelocity());
		this.gameBackground.getLastChild().attachChild(upknife);

		knifelr = new Sprite(0, 0, knifeLRTextureRegion,
				this.getVertexBufferObjectManager());
		knifelr.setPosition(20, 40);
		knifelrBody = PhysicsFactory.createBoxBody(level3PhysicsWorld, knifelr,
				BodyType.KinematicBody, fixDef);
		knifeObject = makeJsonData(knifeID, knifelrBody, knifelr);
		knifelrBody.setUserData(knifeObject);
		knifelrBody.setLinearVelocity(70.0f, 0);
		direcLR = 1;
		level3PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				knifelr, knifelrBody, true, false));
		this.gameBackground.registerUpdateHandler(this.setLRVelocity());
		this.gameBackground.getLastChild().attachChild(knifelr);

		knifelr2 = new Sprite(0, 0, knifeLRTextureRegion,
				this.getVertexBufferObjectManager());
		knifelr2.setPosition(4, 445);
		knifelrBody2 = PhysicsFactory.createBoxBody(level3PhysicsWorld,
				knifelr2, BodyType.KinematicBody, fixDef);
		knifeObject = makeJsonData(knifeID, knifelrBody2, knifelr2);
		knifelrBody2.setUserData(knifeObject);
		knifelrBody2.setLinearVelocity(-70.0f, 0);
		direcLR2 = 2;
		level3PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				knifelr2, knifelrBody2, true, false));
		this.gameBackground.registerUpdateHandler(this.setLRVelocity2());
		this.gameBackground.getLastChild().attachChild(knifelr2);

		this.level3PhysicsWorld.setContinuousPhysics(true);
		this.level3PhysicsWorld.setContactListener(new ContactListener() {

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
							rollerID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(rollerID))) {

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(rollerID)) {

							bodyA.setType(BodyType.StaticBody);

						} else {

							bodyB.setType(BodyType.StaticBody);
						}

						if (audioOptions.getBoolean("effectsOn", true)) {

							Level3Activity.mGrenade.play();
							mHandler.postDelayed(mPlayGameOverSound, 2000);
							mHandler.postDelayed(mTry, 3500);
						}

						else {
							mEngine.setScene(gameOverScene);
							mHandler.postDelayed(mTry, 1500);
						}

					} else if ((obA.get("ID").equals(heroID) && obB.get("ID")
							.equals(enemyID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(enemyID))) {

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(enemyID)) {

							bodyA.setType(BodyType.StaticBody);

						} else {

							bodyB.setType(BodyType.StaticBody);
						}

						if (audioOptions.getBoolean("effectsOn", true)) {

							Level3Activity.mExplosion.play();
							mHandler.postDelayed(mPlayGameOverSound, 2000);
							mHandler.postDelayed(mTry, 3500);
						}

						else {
							mEngine.setScene(gameOverScene);
							mHandler.postDelayed(mTry, 1500);
						}

					} else if ((obA.get("ID").equals(heroID) && obB.get("ID")
							.equals(knifeID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(knifeID))) {

						final Sprite spA = (Sprite) obA.get("sprite");
						final Sprite spB = (Sprite) obB.get("sprite");

						spA.clearUpdateHandlers();
						spB.clearUpdateHandlers();

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(knifeID)) {

							bodyA.setType(BodyType.StaticBody);

							bodyB.setType(BodyType.StaticBody);

						} else {

							bodyB.setType(BodyType.StaticBody);

							bodyA.setType(BodyType.StaticBody);
						}

						if (audioOptions.getBoolean("effectsOn", true)) {

							Level3Activity.mStab.play();
							mHandler.postDelayed(mPlayGameOverSound, 2100);
							mHandler.postDelayed(mTry, 3500);
						}

						else {
							mEngine.setScene(gameOverScene);
							mHandler.postDelayed(mTry, 1500);
						}

					} else if ((obA.get("ID").equals(heroID) && obB.get("ID")
							.equals(finishID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(finishID))) {

						Log.i(TAG,
								"entered contact method between finish and hero");
						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(finishID)) {

							bodyA.setType(BodyType.StaticBody);

						} else if (obB.get("ID").equals(heroID)
								&& obA.get("ID").equals(finishID)) {

							bodyB.setType(BodyType.StaticBody);

						}

						final String scorenow;
						scorenow = Integer.toString(scoresOptions.getInt(
								"score", 0));

						if (highScores < scoresOptions.getInt("score", 0)) {
							scoresEditor.putInt("Level1", highScores);
							scoresEditor.commit();
						}
						showScoreText.setText(scorenow);
						// If current score is greater than high score

						Log.i(TAG, "checking if effectson is true");
						if (audioOptions.getBoolean("effectsOn", true)) {

							Log.i(TAG, "effectson is true");
							Level3Activity.mApplause.play();
							// background.clearChildScene();
							mEngine.setScene(popUp);
							mHandler.postDelayed(mCoinSceneAdd, 3500);
							mHandler.postDelayed(mEndGame, 6500);

						} else {
							Log.i(TAG, "effectson is false");
							mEngine.setScene(popUp);
							mHandler.postAtTime(mCoinSceneAdd, 3500);
							mHandler.postAtTime(mEndGame, 6500);
						}

					} else if ((obA.get("ID").equals(heroID) && obB.get("ID")
							.equals(coinID))
							|| (obB.get("ID").equals(heroID) && obA.get("ID")
									.equals(coinID))) {

						Log.i(TAG,
								"entered contact method between coin and hero");

						coinScoreAdd += COIN_SCORE;
						scoresEditor.putInt("score", coinScoreAdd);
						scoresEditor.commit();

						Log.i(TAG, "setting delete status as true");

						if (obA.get("ID").equals(heroID)
								&& obB.get("ID").equals(coinID)) {

							obB.put("deleteStatus", true);

						} else if (obB.get("ID").equals(heroID)
								&& obA.get("ID").equals(coinID)) {

							obA.put("deleteStatus", true);

						}

						Log.i(TAG, "checking if effectsOn is true");
						if (audioOptions.getBoolean("effectsOn", true)) {

							Level3Activity.mMunch.play();

						}

					}
				} catch (JSONException e) {
					// TODO: handle exception
					Log.e(TAG, "error at contact listener");
				}
			}
		});
		this.gameBackground.registerUpdateHandler(this
				.removeObjectUpdatehandler());
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	private IUpdateHandler setLRVelocity2() {
		// TODO Auto-generated method stub
		return new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub

				if (direcLR2 == 1) {
					if (knifelr2.getX() >= endingLRY) {
						direcLR2 = 2;
						knifelrBody2.setLinearVelocity(
								knifelrBody2.getLinearVelocity().x * -1, 0);
					}
				} else if (direcLR2 == 2) {

					if (knifelr2.getX() <= startingLRY) {
						direcLR2 = 1;
						knifelrBody2.setLinearVelocity(
								knifelrBody2.getLinearVelocity().x * -1, 0);
					}

				}
			}
		};
	}

	private IUpdateHandler removeObjectUpdatehandler() {

		return new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub

				JSONObject currentJsonObj;

				for (int i = 0; i < Level3Activity.this.deletejsonList.size(); i++) {

					currentJsonObj = Level3Activity.this.deletejsonList.get(i);

					try {

						if (currentJsonObj.getBoolean("deleteStatus")) {
							Log.i(TAG, "Entered method to remove coin");

							final AnimatedSprite Object = (AnimatedSprite) currentJsonObj
									.get("sprite");

							currentJsonObj.put("deleteStatus", false);
							final Body body = (Body) currentJsonObj.get("body");
							level3PhysicsWorld.destroyBody(body);
							// scene.detachChild(Object);
							Object.setVisible(false);
							Log.i(TAG, "Entered method to remove coin");
						}
					} catch (JSONException e) {
						// TODO: handle exception
						Log.e(TAG,
								"exception inside update handler removeobjectupdate handler");
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
			Level3Activity.mGameOver.play();
		}
	};

	private Runnable mEndGame = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Level3Activity.this.finish();
			Intent maintent = new Intent(Level3Activity.this,
					MenuActivity.class);
			startActivity(maintent);
		}
	};

	private Runnable mTry = new Runnable() {
		public void run() {

			// mEngine.getScene().back();
			Intent tryIntent = new Intent(Level3Activity.this, TryAgain.class);
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
		this.level3PhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	private void addEnemySaw(final float pX, final float pY) {
		// TODO Auto-generated method stub
		final AnimatedSprite enemySaw;
		final Body enemyBody;
		enemySaw = new AnimatedSprite(pX, pY,
				enemyAnimTextureRegion.deepCopy(),
				this.getVertexBufferObjectManager());
		enemyBody = PhysicsFactory.createCircleBody(level3PhysicsWorld,
				enemySaw, BodyType.StaticBody,
				PhysicsFactory.createFixtureDef(0, 0, 0));
		enemyObject = makeJsonData(enemyID, enemyBody, enemySaw);
		enemyBody.setUserData(enemyObject);
		level3PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				enemySaw, enemyBody, true, false));
		this.gameBackground.getLastChild().attachChild(enemySaw);
		enemySaw.animate(100);
	}

	private void addCoin(final float pX, final float pY) {
		// TODO Auto-generated method stub
		final AnimatedSprite coin;
		final Body coinBody;
		coin = new AnimatedSprite(pX, pY, coinTextureRegion.deepCopy(),
				this.getVertexBufferObjectManager());
		coinBody = PhysicsFactory.createCircleBody(level3PhysicsWorld, coin,
				BodyType.StaticBody, PhysicsFactory.createFixtureDef(0, 0, 0));
		level3PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coin,
				coinBody, true, false));
		this.gameBackground.getLastChild().attachChild(coin);
		coinObject = makeJsonData(coinID, coinBody, coin);
		deletejsonList.add(coinObject);
		coinBody.setUserData(coinObject);
		coin.animate(100);

	}

	private void addRoller(final float pX, final float pY) {
		// TODO Auto-generated method stub
		final AnimatedSprite roller;
		final Body body;
		final FixtureDef fixDef;
		roller = new AnimatedSprite(0, 0, rollerTextureRegion.deepCopy(),
				this.getVertexBufferObjectManager());
		roller.setPosition(pX, pY);
		fixDef = PhysicsFactory.createFixtureDef(0, 0, 0);
		body = PhysicsFactory.createBoxBody(level3PhysicsWorld, roller,
				BodyType.StaticBody, fixDef);
		rollerObject = makeJsonData(rollerID, body, roller);
		body.setUserData(rollerObject);
		roller.animate(100);
		level3PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				roller, body, true, false));
		this.gameBackground.getLastChild().attachChild(roller);
	}

	private IUpdateHandler setUpVelocity() {
		// TODO Auto-generated method stub
		return new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub
				if (direcUp == 2) {
					if (upknife.getY() <= endingUpY) {
						direcUp = 1;
						// upknife.setFlippedVertical(true);
						knifeUpbody.setLinearVelocity(0,
								knifeUpbody.getLinearVelocity().y * -1);
					}

				} else if (direcUp == 1) {
					if (upknife.getY() >= startingUpY) {

						direcUp = 2;
						// upknife.setFlippedVertical(true);
						knifeUpbody.setLinearVelocity(0,
								knifeUpbody.getLinearVelocity().y * -1);
					}

				}
			}
		};
	}


	private IUpdateHandler setDownVelocity() {
		// TODO Auto-generated method stub
		return new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub

				if (direcDown == 1) {
					if (knife.getY() >= endingY) {
						direcDown = 2;
						// knife.setFlippedVertical(true);
						knifeDownbody.setLinearVelocity(0,
								knifeDownbody.getLinearVelocity().y * -1);
					}
				} else if (direcDown == 2) {

					if (knife.getY() <= startingY) {
						direcDown = 1;
						// knife.setFlippedVertical(true);
						knifeDownbody.setLinearVelocity(0,
								knifeDownbody.getLinearVelocity().y * -1);
					}

				}
			}
		};
	}

	private IUpdateHandler setLRVelocity() {
		// TODO Auto-generated method stub
		return new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub

				if (direcLR == 1) {
					if (knifelr.getX() >= endingLRY) {
						direcLR = 2;
						// knife.setFlippedVertical(true);
						knifelrBody.setLinearVelocity(
								knifelrBody.getLinearVelocity().x * -1, 0);
					}
				} else if (direcLR == 2) {

					if (knifelr.getX() <= startingLRY) {
						direcLR = 1;
						// knife.setFlippedVertical(true);
						knifelrBody.setLinearVelocity(
								knifelrBody.getLinearVelocity().x * -1, 0);
					}

				}
			}
		};
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

}
