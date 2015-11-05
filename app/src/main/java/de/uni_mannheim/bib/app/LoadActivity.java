/*
 * Copyright (C) 2014 Universitätsbibliothek Mannheim
 *
 * Author:
 *    Alexander Wagner <alexander.wagner@bib.uni-mannheim.de>
 * 
 * 
 * This is free software licensed under the terms of the GNU GPL, 
 * version 3, or (at your option) any later version.
 * See <http://www.gnu.org/licenses/> for more details.
 *
 *
 * Shows wlan load of library sections as a list. 
 * This may be online or database content.
 * 
 * 
 */

package de.uni_mannheim.bib.app;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class LoadActivity extends ActionBarActivity {

	private boolean log_enabled = false;

	private LoadActivity la = this;
	private GridView gv;
	// private Menu menu;
	
	Context context;
	ProgressDialog progressBar;
	private int progressBarStatus = 0;
	private Handler progressBarHandler = new Handler();

	private String db_mode_on;
	public static List<Load> dsLoad;
	private String network_state;
	private boolean srcFromWWW;
	private boolean srcFromDB;
	
	private Date date;
	private Calendar cal;
	private String strdate;

	boolean db_empty = true;
	
	String[] loadList = new String[6];
	String[] resIdList = new String[6];
	String[] nameList = new String[6];

	@Override
	public void onCreate(Bundle savedInstanceState) {

		ActivityRegistry.register(this);
		
		srcFromWWW = false;
		srcFromDB = false;
		
		if(log_enabled) {
			Log.e(this.getClass().getName().toUpperCase().toString(), " ... LOADED");
		}
		
		// Toast.makeText(getApplicationContext(), this.getClass().getName(), 
				// Toast.LENGTH_SHORT).show();
		
		// screen layout for main activity
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);

		// customized actionbar (Color, Title)
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#990000")));
		actionBar.setTitle("UB Mannheim");

		// create refresh button
		final ImageView imageView8 = (ImageView) findViewById(R.id.imageView8);

		imageView8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				NetworkChecker nc = new NetworkChecker();
				
				SharedPreferences settings = LoadActivity.this.getSharedPreferences(
						"preferences", 0);
				SharedPreferences.Editor preferencesEditor = settings.edit();
	
				if (nc.isConnected(LoadActivity.this.getApplicationContext())) {

					preferencesEditor.putString("NetworkConnectionAvailable",
							"true");
					preferencesEditor.commit();
				} else {
					preferencesEditor.putString("NetworkConnectionAvailable",
							"false");
					preferencesEditor.commit();
				}
				
				// Network State
				network_state = settings.getString(
						"NetworkConnectionAvailable", "false");

				// DEBUG
				// network_state="false";

				if (network_state.equals("false")) {
					new AlertDialog.Builder(LoadActivity.this)
							.setTitle("Laden der Daten nicht m�glich")
							.setMessage(
									"Um die Anzeige zu aktualisieren "
											+ "ist eine Verbindung zum Internet "
											+ "n�tig.")
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// do nothing
										}
									}).show();
				} else {
					finish();
					startActivity(getIntent());
				}
			}
		});

		gv = (GridView) this.findViewById(R.id.gridView1);

	}

	@Override
	public void onStart() {

		// Log Message
		if (log_enabled) {
			Log.e(this.getClass().toString(), "onStart");
		}

		// prepare for a progress bar dialog
		progressBar = new ProgressDialog(gv.getContext());
		// progressBar.setCancelable(true);
		progressBar.setMessage(getString(R.string.loading_de));
		// progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// progressBar.setProgress(0);
		// progressBar.setMax(100);
		progressBar.show();

		// reset progress bar status
		progressBarStatus = 0;

		// GridView invisible until content is loaded
		gv.setVisibility(View.GONE);

		// progressBar = ProgressDialog.show(this, loading, "", true, false);

		new Thread(new Runnable() {
			@Override
			public void run() {
				
				dsLoad = null;

				// Log Message
				if (log_enabled) {
					Log.e(this.getClass().toString(), "Start inner Thread ...");
				}

				// get Preferences
				SharedPreferences settings = getSharedPreferences(
						"preferences", 0);

				// Network State
				network_state = settings.getString(
						"NetworkConnectionAvailable", "false");
				Helper h = new Helper();
				String network_helper_state = h
						.getNetworkConnectionState(network_state);

				// DB Mode
				db_mode_on = settings.getString("Config_DatabaseModeOn",
						"false");

				// DB Helper
				DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());

				// Data Collector
				DataCollector dc = new DataCollector();

/**
* ****************************************************************************
* loading
* ****************************************************************************
*/

				// Actions in loop
				while (progressBarStatus < 100) {

// Log.e(" ---------- DEBUG ---------- ", "While progressBarStatus loading");
					
					// Log Message
					if (log_enabled) {
						Log.e(this.getClass().toString(),
								String.valueOf(progressBarStatus));
					}

					// if computer is too fast, sleep 1 second to see dialog
					try {
						// Thread.sleep(1000);
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

		/**
		 * ****************************************************************************
		 * network_state.equals("true")
		 * ****************************************************************************
		 */		
					
					// Get Load from Online Resource
					List<Load> loadWWW = null;
					if (network_state.equals("true")) {
				
						// Log.e(" ---------- DEBUG ---------- ", "network_state = " + network_state);
						
						loadWWW = dc.getLoadFromWWW();
						srcFromWWW = true;
						
						strdate = "";
						strdate = getFormattedDate();
						
						// Log.e(" ---------- DEBUG ---------- ", "WWW Date sets tv to " + strdate);
					}

		/**
		 * ****************************************************************************
		 * db_mode_on.equals("true")
		 * ****************************************************************************
		*/
					
					// Get Load from DB
					List<Load> loadDB = null;
					if (db_mode_on.equals("true")) {
						
					// Log.e(" ---------- DEBUG ---------- ", "db_mode = on");
					
						if (log_enabled) {
							Log.e(this.getClass().toString(),
									"loadDB pending ...");
						}
						loadDB = dc.getLoadFromDB(dbh);
						srcFromDB = true;
						
						DatabaseHelper db = new DatabaseHelper(
								LoadActivity.this);
						
						List<History> hList = db.getAllHistory();
						
						if(network_state.equals("false")) {
							strdate = "";
							strdate = getFormattedDateFromDB(hList.get(1).last_update);
							
							// Log.e(" ---------- DEBUG ---------- ", "DB Date sets tv to " + strdate);
						}
					}
		
		/**
		 * ****************************************************************************
		 * dsLoad empty ?
		 * ****************************************************************************
		*/
					// until dsLoad is complete
					if (dsLoad == null) {

					// Log.e(" ---------- DEBUG ---------- ", "dataset <dsLoad> empty");
						
						// Create Global Load List
						dsLoad = dc.getLoadAccordingToSyncState(
								getApplicationContext(), network_helper_state,
								db_mode_on, dbh, loadWWW, loadDB);

						// if DB is Off
						if (db_mode_on.equals("false")) {
						
							// Log.e(" ---------- DEBUG ---------- ", "dataset <dsLoad> empty && db_mode off");
							
							try {
								// Thread.currentThread().interrupt();
								Thread.currentThread().setPriority(
										Thread.MIN_PRIORITY);
								progressBarStatus += 1;
							} catch (Exception e) {
								Log.e("", "Error on interrupt " + e);
							}
						}
						

					} else {
						
				// Log.e(" ---------- DEBUG ---------- ", "dataset <dsLoad> filled");
						
						// count up progressBar
						// progressBarStatus += 1;

						// set progressBar=100
						progressBarStatus = 100;

						// Log Message
						if (log_enabled) {
							Log.e(this.getClass().toString(),
									String.valueOf(progressBarStatus));
						}

					}
					
					

					// Update the progress bar
					progressBarHandler.post(new Runnable() {
						@Override
						public void run() {

							// Log Message
							if (log_enabled) {
								Log.e(this.getClass().toString(),
										"set progressBarStatus");
							}
							progressBar.setProgress(progressBarStatus);
						}
					});

				} // while end

/**
 * ****************************************************************************
 * loading finished
 * ****************************************************************************
*/
				
				// ok, action is fullfilled
				if (progressBarStatus >= 100) {
				
// Log.e(" ---------- DEBUG ---------- ", "progressBarStatus loaded (loading finished)");
					
					// Log Message
					if (log_enabled) {
						Log.e(this.getClass().toString(), "ProgressBar Full");
					}

					// sleep some seconds, so that you can see the 100%
					try {
						// Thread.sleep(2000);
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// If Global Load available
					if (dsLoad != null) {

// Log.e(" ---------- DEBUG ---------- ", "progessBar full && dataset <dsLoad> filled");
						
						// Log Message
						if (log_enabled) {
							int s = dsLoad.size();
							Log.e("SizeOf dsLoad:", String.valueOf(s));

							int i = 0;

							for (Load d : dsLoad) {
								Log.e("dsLoad: ", String.valueOf(d.getId())
										+ " - " + String.valueOf(d.getLoad())
										+ " - " + d.getSector());
							}
						}

						// If DB Mode on update History
						if (db_mode_on.equals("true") &&  (srcFromWWW==true)) {
							
// Log.e(" ---------- DEBUG ---------- ", "dataset <dsLoad> filled && db_mode = on");
							
							History hload = new History(2, 2, dbh.getDateTime());
							dbh.updateHistory(hload);
							
							// Log.e(" ---------- DEBUG ---------- ", "updating time in db");
						}

// Log.e(" ---------- DEBUG ---------- ", "fill temporary List");

						
						
						// Fill Temporary Lists
						for (int i = 0; i < dsLoad.size(); i++) {


							int load = dsLoad.get(i).getLoad();
							loadList[i] = String.valueOf(load);

							int id = setLight(dsLoad.get(i).getLoad());
							resIdList[i] = String.valueOf(id);

							String name = dsLoad.get(i).getSector();
							nameList[i] = name;

// Log.e(" ---------- DEBUG ---------- ", String.valueOf(i) + " >> " + load + " | " +  name);
// Log.e(" ---------- DEBUG ---------- ", "network_state: "+network_state + " db_mode_on: "+ db_mode_on);
							
							if(load!=-1) {
								db_empty = false;
							}

							if(i==5 && db_empty) {
// Log.e(" ---------- DEBUG ---------- ", "db_empty");
								/*								
								Looper.prepare();
								Looper.loop();
								*/
								/*
								
								*/
							}
						}

						// Fill Adapter
						gv.post(new Runnable() {
							@Override
							public void run() {

								// Log Message
								if (log_enabled) {
									Log.e(this.getClass().toString(), "GET 0"
											+ loadList[1]);
								}

// Log.e(" ---------- DEBUG ---------- ", "Starting inner runnable");
								
								// if (((loadList[0].length() == 0) && (db_mode_on
								//		.equals("true")) && !(network_state.equals("true"))) || (
								
								if ( network_state.equals("false") && db_mode_on.equals("true") && 
																
								// network_state.equals("false")
								// Timestamp in DB is initial Timestamp
								
								
										// Init Entries : load = null
										((loadList[0].equals("-1"))
												&& (loadList[1].equals("-1"))
												&& (loadList[2].equals("-1"))
												&& (loadList[3].equals("-1"))
												&& (loadList[4].equals("-1")) 
												&& (loadList[5].equals("-1")))
												//)

								) {
								
// Log.e(" ---------- DEBUG ---------- ", "(loadList = 0 && db_mode = on && network_state=true) OR (loadList elements = -1)");

									new AlertDialog.Builder(
											LoadActivity.this)
											.setTitle(
													"Keine Daten im Cache vorhanden")
											.setMessage(
													"Um freie Pl�tze anzeigen zu k�nnen "
															+ "ist eine initiale Verbindung zum Internet "
															+ "n�tig. Sie werden nun zum Startmenu "
															+ "weitergeleitet.")
											.setPositiveButton(
													"Ok",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															Intent intent = new Intent(
																	LoadActivity.this,
																	MainActivity.class);
															startActivity(intent);
														}
													}).show();

// Log.e(" ---------- DEBUG ---------- ", network_state);									
// Log.e(" ---------- DEBUG ---------- ", "dialog shown, (BIG IF)");

								} else {
									
// Log.e(" ---------- DEBUG ---------- ", "filling gridview (ELSE BIG IF)");
									
									gv.setAdapter(new LoadAdapter(la, loadList,
											resIdList, nameList));

									TextView tv2 = (TextView) findViewById(R.id.textView2);
									tv2.setText("");
									tv2.setText(strdate);

// Log.e(" ---------- DEBUG ---------- ", "gridview filled");				

									// Close Loading Dialog
									// progressBar.dismiss();
									// GridView visibility on
									gv.setVisibility(View.VISIBLE);
								}
							}

						});

					// If exist Global Load NOT available
					} else {

// Log.e(" ---------- DEBUG ---------- ", "dataset <dsLoad> filled");
						
						gv.post(new Runnable() {
							@Override
							public void run() {

								// Close Loading Dialog
								progressBar.dismiss();

								new AlertDialog.Builder(LoadActivity.this)
										.setTitle(
												"Verbindungsfehler")
										.setMessage(
												"Bei nicht aktiviertem Daten-Cache wird eine bestehende "
												+ "Internetverbindung zur Darstellung der Funktion News "
												+ "ben�tigt. Bitte verbinden Sie Ihr Ger�t mit dem Internet "
												+ "und versuchen Sie es erneut. Sie werden nun zum "
												+ "Startmen� weitergeleitet.")
										.setPositiveButton(
												"Ok",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														Intent intent = new Intent(
																LoadActivity.this,
																MainActivity.class);
														startActivity(intent);
													}
												}).show();

							}
						});
					}

					// Close Loading Dialog
					progressBar.dismiss();

				} // if progressBarStatus >= 100

			} // end void run

		}).start();

		super.onStart();

	}


	/*
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) {
	 * 
	 * switch (item.getItemId()) { case android.R.id.home: // This ID represents
	 * the Home or Up button. In the case of this // activity, the Up button is
	 * shown. Use NavUtils to allow users // to navigate up one level in the
	 * application structure. For // more details, see the Navigation pattern on
	 * Android Design: // //
	 * http://developer.android.com/design/patterns/navigation.html#up-vs-back
	 * // NavUtils.navigateUpFromSameTask(this);
	 * 
	 * return true;
	 * 
	 * } return super.onOptionsItemSelected(item);
	 * 
	 * }
	 */

	private int setLight(int percent) {

		int lights;

		// Log Message
		if (log_enabled) {
			Log.e("SET_COLOR", String.valueOf(percent));
		}
/*
		if (percent < 61) {
			lights = R.drawable.ampel_gruen_42x14;
		} else if (percent < 81) {
			lights = R.drawable.ampel_gelb_42x14;
		} else {
			lights = R.drawable.ampel_rot_42x14;
		}
*/
		if (percent < 61) {
			lights = R.drawable.sign_green;
		} else if (percent < 81) {
			lights = R.drawable.sign_yellow;
		} else {
			lights = R.drawable.sign_red;
		}
		
		return lights;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_config:
			Intent myIntent0 = new Intent(LoadActivity.this,
					ConfigActivity.class);
			LoadActivity.this.startActivity(myIntent0);
			return true;
		case R.id.action_help:
			Intent myIntent1 = new Intent(LoadActivity.this,
					HelpActivity.class);
			LoadActivity.this.startActivity(myIntent1);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	public String getFormattedDate(){
		
		String dat = "";
		
		date = new Date();
		cal = Calendar.getInstance();
		cal.setTime(date);
		
		int year = cal.get(Calendar.YEAR);
		// Hint: month array starting with 0 (January = 0)
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DATE);
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		
		String month_string = "";
		month_string = String.valueOf(month + 1);
		
		String min_string = String.valueOf(min);
		if(min<9) {
			min_string = "0" + String.valueOf(min);
		}
		
		dat = day + "." + month_string + "."
				+ String.valueOf(year).substring(2) + ", "
				+ hours + ":" + min_string + " Uhr";
		
		return dat;
	}

	public String getFormattedDateFromDB(String timestamp) {
		String[] stamp = timestamp.split(" ");
		String[] date = stamp[0].split("-");
		String[] time = stamp[1].split(":");

		String day = date[2];
		if(Integer.parseInt(day)<9) {
			// cuts leading 0
			day = day.substring(1);
		}
		
		String formatted_date = day + "." + date[1] + "."
				+ date[0] + ", " + time[0] + ":" + time[1];

		return formatted_date;
	}
	
	@Override
	public void onBackPressed() {
		// exits app, no more activites in stack?
		// moveTaskToBack(true);
		
		NavUtils.navigateUpFromSameTask(this);
        
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		
		// Log.e("--------XXXXXXXXXXXXXXXXx-----------", "onRestart");
		SharedPreferences settings = LoadActivity.this.getSharedPreferences(
				"preferences", 0);
		
		NetworkChecker nc = new NetworkChecker();
		boolean netstat = nc.isConnected(LoadActivity.this);
		
		// Log.e("NETSTAT", String.valueOf(netstat));
		
		if(netstat) {
			SharedPreferences.Editor preferencesEditor = settings.edit();
			preferencesEditor.putString("NetworkConnectionAvailable",
					"true");
			preferencesEditor.commit();
		}
		else {
			SharedPreferences.Editor preferencesEditor = settings.edit();
			preferencesEditor.putString("NetworkConnectionAvailable",
					"false");
			preferencesEditor.commit();
			/*
			new AlertDialog.Builder(LoadActivity.this)
			.setTitle(
					"Verbindungsfehler")
			.setMessage(
					"F�r diese Anwendung ist eine Datenverbindung erforderlich. "
						+ "Atkivieren Sie Mobilfunk oder Wi-Fi, um Daten "
						+ "herunterzuladen.")
			.setPositiveButton(
					"Einstellungen",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
							
							startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
							finish();
						}
					})
			.setNegativeButton("Abbrechen",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
			
						}
					}).show();
			*/
		}
	}

}