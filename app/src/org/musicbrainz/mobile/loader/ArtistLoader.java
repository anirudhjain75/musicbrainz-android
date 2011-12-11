/*
 * Copyright (C) 2011 Jamie McDonald
 * 
 * This file is part of MusicBrainz for Android.
 * 
 * MusicBrainz for Android is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * MusicBrainz for Android is distributed in the hope that it 
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MusicBrainz for Android. If not, see 
 * <http://www.gnu.org/licenses/>.
 */

package org.musicbrainz.mobile.loader;

import java.io.IOException;

import org.musicbrainz.android.api.data.Artist;
import org.musicbrainz.android.api.data.UserData;
import org.musicbrainz.android.api.util.Credentials;
import org.musicbrainz.android.api.webservice.MBEntity;
import org.musicbrainz.android.api.webservice.WebClient;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class ArtistLoader extends AsyncTaskLoader<AsyncEntityResult<Artist>> {

    private Credentials creds;
    private String userAgent;
    private String mbid;
    
    private AsyncEntityResult<Artist> data;

    public ArtistLoader(Context context, Credentials creds, String mbid) {
        super(context);
        this.creds = creds;
        this.mbid = mbid;
    }

    public ArtistLoader(Context context, String userAgent, String mbid) {
        super(context);
        this.userAgent = userAgent;
        this.mbid = mbid;
    }

    @Override
    public AsyncEntityResult<Artist> loadInBackground() {
        try {
            return getAvailableData();
        } catch (Exception e) {
            return new AsyncEntityResult<Artist>(LoaderStatus.EXCEPTION, e);
        }
    }

    private AsyncEntityResult<Artist> getAvailableData() throws IOException {
        if (creds == null) {
            return getArtist();
        } else {
            return getArtistWithUserData();
        }
    }

    private AsyncEntityResult<Artist> getArtist() throws IOException {
        WebClient client = new WebClient(userAgent);
        Artist artist = client.lookupArtist(mbid);
        data = new AsyncEntityResult<Artist>(LoaderStatus.SUCCESS, artist);
        return data;
    }

    private AsyncEntityResult<Artist> getArtistWithUserData() throws IOException {
        WebClient client = new WebClient(creds);
        Artist artist = client.lookupArtist(mbid);
        UserData userData = client.getUserData(MBEntity.ARTIST, mbid);
        data = new AsyncEntityResult<Artist>(LoaderStatus.SUCCESS, artist, userData);
        return data;
    }
    
    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        }
        if (takeContentChanged() || data == null) {
            forceLoad();
        }
    }
    
    @Override
    public void deliverResult(AsyncEntityResult<Artist> data) {
        if (isReset()) {
            return;
        }
        this.data = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        data = null;
    }

}