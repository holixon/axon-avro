<grid drag="100 10" drop="top" align="center" pad="10px 20px">
 <h1><% title %></h1>
</grid>

<grid drag="30 60" drop="15 15" align="topleft">
<%? left %>
</grid>

<grid drag="30 60" drop="53 15" align="topright">
<%? right %>
</grid>

<% content %>

<grid drag="100 20" drop="bottom" align="center" pad="10px 20px">
 <% footer %>
</grid>
