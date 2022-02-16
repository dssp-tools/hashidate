/**
 *
 */
function init()
{
	version();
    chapter(0);
}

function version(){
	var elm = document.getElementById("version");
	elm.innerHTML = "1.09";
}

function chapter(chapterIndex)
{
    // 章、節、数式、図表を自動番号付け
    var path = new Array("chapter", "section", "subsection");
    var pathIndex = new Array();
    for (i = 0; i < path.length; i++)
    {
	pathIndex[i] = 0;
    }
    var chapContents = new Array();
    var eq = new Array("equation", "figure", "table");
    var eqIndex = new Array();
    for (i = 0; i < eq.length; i++)
    {
	eqIndex[i] = 0;
    }
    pathIndex[0] = chapterIndex;
    var eqCaption = new Array();
    var ps = document.getElementsByTagName("div");
    for (i = 0; i < ps.length; i++)
    {
	var name = ps[i].className;
	for (j = 0; j < path.length; j++)
	{
	    if (name == path[j])
	    {
		pathIndex[j]++;
		header = "";
		for (k = 0; k < j; k++)
		{
		    header += pathIndex[k] + ".";
		}
		switch(j)
		{
		case 0:
		    header += pathIndex[j] + " ";
		    break;
		case 1:
		    header += pathIndex[j] + " ";
		    // 数式番号のリセット
		    for (k = 0; k < eq.length; k++)
		    {
			eqIndex[k] = 0;
		    }
		    break;
		default:
		    header += pathIndex[j] + " ";
		}
		var title = ps[i].innerHTML;
		ps[i].innerHTML = header + title;
		for (k = (j+1) ; k < pathIndex.length; k++)
		{
		    pathIndex[k] = 0;
		}
		break;
	    }
	}
	// 数式、図表番号
	for (j = 0; j < eq.length; j++)
	{
	    if (name == eq[j])
	    {
		eqIndex[j]++;
		header = "";
		for (k = 0; k < pathIndex.length; k++)
		{
		    header += pathIndex[k] + ".";
		}
		switch(j)
		{
		case 0:
		    ps[i].innerHTML = "<center>" + ps[i].innerHTML + "<span class='number'>(" + header + eqIndex[j] + ")</span></center>";
		    break;
		case 1:
		    var caption = ps[i].getAttribute("caption");
		    eqCaption[caption] = "図 " + header + eqIndex[j] + "：" + caption;
		    ps[i].innerHTML = "<center>" + ps[i].innerHTML + "</center><center>" + eqCaption[caption] + "</center>";
		    break;
		case 2:
		    var caption = ps[i].getAttribute("caption");
		    eqCaption[caption] = "表 " + header + eqIndex[j] + "：" + caption;
		    ps[i].innerHTML = "<center>" + ps[i].innerHTML + "</center><center>" + eqCaption[caption] + "</center>";
		    break;
		}
		break;
	    }
	}
    }

    // 参照の解決
    var refs = document.getElementsByTagName("ref");
    for (i = 0; i < refs.length; i++)
    {
	var caption = refs[i].getAttribute("caption");
	refs[i].innerHTML = eqCaption[caption];
    }
}
