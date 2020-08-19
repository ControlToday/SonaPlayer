package com.McDevelopers.sonaplayer.ArtistListActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.McDevelopers.sonaplayer.ApplicationContextProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArtistLibrary {

    /*public static final HashMap<Long, ArrayList<String>> albumList = new HashMap<>();
    public static final  SparseLongArray albumIds = new SparseLongArray();
    public  static final HashMap<Long, ArrayList<String>> songList = new HashMap<>();
    public static final HashMap<Long, ArrayList<String>> songMeta = new HashMap<>();*/

    private static long audioReject=60000;
    public static List<ArtistCategoryExpanded> artistCategory=new ArrayList<>();
    public static List<ArtistCategoryExpanded> albumxDummyData=new ArrayList<>();

    private String songCounts;


    public ArtistLibrary(){
        SharedPreferences currentState = ApplicationContextProvider.getContext().getSharedPreferences("com.McDevelopers.sonaplayer", Context.MODE_PRIVATE);
        audioReject=currentState.getLong("audioReject",60000);
        artistCategory.clear();
        Log.e("ArtistListCleared", "ArtistLibrary: Invoked" );

        // this.getAlbumsLists();

    }


    public void setDummydata(){

        albumxDummyData.clear();

        List<Artists> albumzCategory=new ArrayList<>();

        Artists albumone = new Artists("Album1", "123", "456", "fileAlbum1","albumData1","dummyAlbum",54855,4858);
        albumzCategory.add(albumone);
        ArtistCategoryExpanded album_category_expand = new ArtistCategoryExpanded(1000L,"AlbumGroup1", "4555", albumzCategory, "123","dummyData");
        albumxDummyData.add(album_category_expand);

    }

    public void getAlbumsLists() {

        String where = null;

        final Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        final String _id = MediaStore.Audio.Artists._ID;
        final String album_name = MediaStore.Audio.Artists.ARTIST;
        final String albumart = MediaStore.Audio.Artists.ARTIST_KEY;
        final String tracks = MediaStore.Audio.Artists.NUMBER_OF_TRACKS;
        final String albumSortOrder = MediaStore.Audio.ArtistColumns.ARTIST + " COLLATE LOCALIZED ASC";

        final String[] columns = {_id, album_name, albumart, tracks};
        Cursor cursor = ApplicationContextProvider.getContext().getContentResolver().query(uri, columns, where, null, albumSortOrder);

        if (cursor != null && cursor.moveToFirst()) {

            do {
                long id = cursor.getLong(cursor.getColumnIndex(_id));
                String name = cursor.getString(cursor.getColumnIndex(album_name));
               // String artist2 = cursor.getString(cursor.getColumnIndex(artist));
               // String artPath = cursor.getString(cursor.getColumnIndex(albumart));
               // int nr = cursor.getInt(cursor.getColumnIndex(tracks));

                getSongList(id,name);

               // Log.d("ArtistName", name);

            } while (cursor.moveToNext());
        }

        cursor.close();
    }




    private  void getSongList(long albumID,String Name){



        String selection = "is_music != 0";

        if (albumID > 0) {
            selection = selection + " and artist_id = " + albumID;
        }

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.SIZE
        };
        String albumUri=null;

        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = null;
        String songData=null;
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor =ApplicationContextProvider.getContext().getContentResolver().query(uri, projection, selection, null, sortOrder);
            if (cursor != null) {
                cursor.moveToFirst();
                int position = 1;

                List<Artists> albumzxCategory=new ArrayList<>();

                while (!cursor.isAfterLast()) {

                if(cursor.getLong(5)>=audioReject) {

                    if( cursor.getString(7)!=null && !cursor.getString(7).isEmpty()) {
                        if (checkUriExists(getAlbumUri(cursor.getString(7)))) {
                            albumUri = cursor.getString(7);

                        }
                    }
                    songData=cursor.getString(3);
                    Artists albumone = new Artists(cursor.getString(1), cursor.getString(0), cursor.getString(7), cursor.getString(4), cursor.getString(3),cursor.getString(6),cursor.getLong(5),cursor.getLong(8));
                    albumzxCategory.add(albumone);

                    //Log.d("AlbumURI", "getSongList: URI: "+albumUri);
                }
                    cursor.moveToNext();
                }

                if(albumzxCategory.size()>1){

                    songCounts=albumzxCategory.size()+" "+"Songs";
                }else {

                    songCounts=albumzxCategory.size()+" "+"Song";
                }



                if(albumzxCategory.size()>0) {
                    ArtistCategoryExpanded album_category_one = new ArtistCategoryExpanded(albumID,Name, albumUri, albumzxCategory, songCounts,songData);
                    artistCategory.add(album_category_one);
                }



            }

        } catch (Exception e) {
            Log.e("Media", e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


    }

    private static Uri getAlbumUri(String mediaID) {

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

       // Log.d("AlbumArtUri", ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mediaID)).toString());
        return ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mediaID));

    }


    private boolean checkUriExists(Uri contentUri){

        boolean isFileExist;

        ContentResolver cr = ApplicationContextProvider.getContext().getContentResolver();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cur = cr.query(contentUri, projection, null, null, null);
        if (cur != null) {
            if (cur.moveToFirst()) {
                String filePath = cur.getString(0);

                if (new File(filePath).exists()) {
                    // do something if it exists
                    isFileExist=true;
                } else {
                    // File was not found
                    isFileExist= false;
                }
            } else {
                // Uri was ok but no entry found.
                isFileExist= false;
            }
            cur.close();
        } else {
            // content Uri was invalid or some other error occurred
           isFileExist= false;
        }
       return isFileExist;
    }
}