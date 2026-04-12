package iamd.cassetteplayer.serialize;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import iamd.cassetteplayer.Cassette;
import iamd.cassetteplayer.Cassette.Side;
import iamd.cassetteplayer.CassetteDesign;
import iamd.cassetteplayer.CassetteSide;
import iamd.cassetteplayer.audio.MP3FilePlayerWrapper;
import iamd.cassetteplayer.audio.MP3FilePlayerWrapper.MP3CachedData;
import iamd.cassetteplayer.model.CassetteLibraryDataModel;
import iamd.cassetteplayer.model.CassettePlayerDataModel;
import iamd.ui.ErrorMessage;
import javazoom.jl.decoder.JavaLayerException;

public class CassetteLibrarySerialization
{
    static final private File file = new File(System.getProperty("user.home") + File.separator + ".cassettelibrary.json");

    static public void open(CassettePlayerDataModel playerDataModel, CassetteLibraryDataModel libraryDataModel)
    {
        System.out.println(file);
        
        try (FileReader fr = new FileReader(file))
        {
            CassetteLibrarySerialization.fromJSON(playerDataModel, libraryDataModel, new JSONTokener(fr));
        }
        catch (FileNotFoundException e1)
        {
            // Do nothing
        }
        catch (IOException e1)
        {
            ErrorMessage.showErrorMessage(e1);
        }
    }
    
    static public void save(CassetteLibraryDataModel libraryDataModel)
    {
        try (FileWriter fw = new FileWriter(file))
        {
            fw.write(CassetteLibrarySerialization.toJSON(libraryDataModel).toString(4));
        }
        catch (IOException e1)
        {
            ErrorMessage.showErrorMessage(e1);
        }
    }

    static private JSONObject createSide(CassetteSide cassetteSideA)
    {
        JSONArray trackList = new JSONArray();
        
        for (MP3FilePlayerWrapper filePlayer : cassetteSideA.getAllAudioPlayers())
        {
            if (filePlayer.file != null)
                trackList.put(filePlayer.getKnownFile().toJSONObject());
        }

        JSONObject side = new JSONObject();
        side.put("headerLine", cassetteSideA.getHeaderLine());
        side.put("bodyText",   cassetteSideA.getBodyText());
        side.put("tracks", trackList);
        return side;
    }

    static private JSONArray toJSON(CassetteLibraryDataModel libraryDataModel)
    {
        JSONArray cassettes = new JSONArray();

        for (iamd.cassetteplayer.Cassette inputCassette : libraryDataModel.getCassettes())
        {
            JSONObject design = new JSONObject();
            design.put("type", inputCassette.getDesignDataModel().getDesign().name());
            design.put("headerFontFamily", inputCassette.getDesignDataModel().getHeaderFontFamily());
            design.put("bodyTextFontFamily", inputCassette.getDesignDataModel().getBodyTextFontFamily());
            design.put("caseColor", colorToString(inputCassette.getDesignDataModel().getCaseColor()));
            
            JSONArray labelColors = new JSONArray();
            for (Color color : inputCassette.getDesignDataModel().getLabelColor())
                labelColors.put(colorToString(color));
            design.put("labelColor", labelColors);
            design.put("spoolColor", colorToString(inputCassette.getDesignDataModel().getSpoolColor()));
            design.put("textColor", colorToString(inputCassette.getDesignDataModel().getTextColor()));

            JSONObject status = new JSONObject();
            status.put("side", inputCassette.getCurrentSide().name());
            status.put("progress" , inputCassette.getProgress());
            
            JSONObject cassette = new JSONObject();
            cassette.put("design", design);
            cassette.put("status", status);
            cassette.put("sideA", createSide(inputCassette.getCassetteSide(iamd.cassetteplayer.Cassette.Side.A)));
            cassette.put("sideB", createSide(inputCassette.getCassetteSide(iamd.cassetteplayer.Cassette.Side.B)));

            cassettes.put(cassette);
        }
        
        JSONObject directory = new JSONObject();
        directory.put("name", "default");
        directory.put("cassettes", cassettes);
        
        JSONArray library = new JSONArray();
        library.put(directory);
        return library;
    }

    static private void fromJSON(CassettePlayerDataModel playerDataModel, CassetteLibraryDataModel libraryDataModel, JSONTokener jsonTokener)
    {
        JSONArray library;
        
        try
        {
            library = new JSONArray(jsonTokener);
        }
        catch(JSONException e)
        {
            return;
        }
        
        JSONObject directory = library.optJSONObject(0);
        
        if (directory == null)
            return;
        
        for (Object cassetteObject : directory.getJSONArray("cassettes"))
        {
            try
            {
                JSONObject cassette = (JSONObject) cassetteObject;
                
                JSONObject sideA = cassette.getJSONObject("sideA");
                JSONObject sideB = cassette.getJSONObject("sideB");
                
                ArrayList<MP3CachedData> audioFilesA = new ArrayList<>();
                for (Object trackObject : sideA.getJSONArray("tracks"))
                    audioFilesA.add(new MP3CachedData((JSONObject) trackObject));
                
                ArrayList<MP3CachedData> audioFilesB = new ArrayList<>();
                for (Object trackObject : sideB.getJSONArray("tracks"))
                    audioFilesB.add(new MP3CachedData((JSONObject) trackObject));

                iamd.cassetteplayer.Cassette inputCassette = new Cassette(playerDataModel, audioFilesA, audioFilesB);

                JSONObject design = cassette.getJSONObject("design");
                inputCassette.getDesignDataModel().setDesign(CassetteDesign.valueOf(design.getString("type")));
                inputCassette.getDesignDataModel().setHeaderFontFamily(design.getString("headerFontFamily"));
                inputCassette.getDesignDataModel().setBodyTextFontFamily(design.getString("bodyTextFontFamily"));
                inputCassette.getDesignDataModel().setCaseColor(stringToColor(design.getString("caseColor")));
                
                Color labelColors[] = new Color[design.getJSONArray("labelColor").length()];
                int i = 0;
                for (Object object : design.getJSONArray("labelColor"))
                    labelColors[i ++] = stringToColor((String) object);
                inputCassette.getDesignDataModel().setLabelColor(labelColors);
                inputCassette.getDesignDataModel().setSpoolColor(stringToColor(design.getString("spoolColor")));
                inputCassette.getDesignDataModel().setTextColor(stringToColor(design.getString("textColor")));

                JSONObject status = cassette.getJSONObject("status");
                
                inputCassette.setCurrentSide(Side.valueOf(status.getString("side")));
                inputCassette.setProgress(status.getDouble("progress"));
                
                inputCassette.getCassetteSide(Side.A).setHeaderLine(sideA.getString("headerLine"));
                inputCassette.getCassetteSide(Side.B).setHeaderLine(sideB.getString("headerLine"));
                inputCassette.getCassetteSide(Side.A).setBodyText(sideA.getString("bodyText"));
                inputCassette.getCassetteSide(Side.B).setBodyText(sideB.getString("bodyText"));
                
                libraryDataModel.addCassette(inputCassette);
            }
            catch (IOException | JavaLayerException | CannotReadException
                    | TagException | ReadOnlyFileException
                    | InvalidAudioFrameException | JSONException | InterruptedException e)
            {
                ErrorMessage.showErrorMessage(e);
            }
        }
    }

    private static String colorToString(Color labelColor)
    {
        return String.format("#%02X%02X%02X", labelColor.getRed(), labelColor.getGreen(), labelColor.getBlue());
    }

    private static Color stringToColor(String colorString)
    {
        int r = Integer.parseInt(colorString.substring(1, 3), 16);
        int g = Integer.parseInt(colorString.substring(3, 5), 16);
        int b = Integer.parseInt(colorString.substring(5), 16);
        
        return new Color(r, g, b);
    }
}
