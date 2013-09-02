/*
 *  Copyright (C) 2010-2013 Axel Morgner, structr <structr@structr.org>
 *
 *  This file is part of structr <http://structr.org>.
 *
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */

var pages;
var previews, previewTabs, controls, palette, activeTab, activeTabRight, components, elements, widgetsSlideout;
var selStart, selEnd;
var sel;
var contentSourceId, elementSourceId, rootId;
var textBeforeEditing;
var activeTabKey = 'structrActiveTab_' + port;
var activeTabRightKey = 'structrActiveTabRight_' + port;
var win = $(window);
var sorting = false;
var sortParent;

$(document).ready(function() {
    Structr.registerModule('pages', _Pages);
    Structr.classes.push('page');

    win.resize(function() {
        _Pages.resize();
    });

    _Pages.makeMenuDroppable();
});

var _Pages = {
    icon: 'icon/page.png',
    add_icon: 'icon/page_add.png',
    delete_icon: 'icon/page_delete.png',
    clone_icon: 'icon/page_copy.png',
    init: function() {

        Structr.initPager('Page', 1, 25);
        Structr.initPager('File', 1, 25);
        _Pages.resize();

    },
    resize: function() {

        var windowWidth = win.width();
        var windowHeight = win.height();
        var headerOffsetHeight = 100;
        var previewOffset = 22;

        //var compTabsHeight = $('#compTabs').height();
        //console.log(pages, palette, previews);

        if (pages && palette) {

            pages.css({
                width: Math.max(180, Math.min(windowWidth / 3, 360)) + 'px',
                height: windowHeight - headerOffsetHeight + 'px'
            });

            var rw = pages.width() + 74;

//            palette.css({
//                width: Math.min(300, Math.max(360, windowWidth / 4)) + 'px',
//                height: windowHeight - (headerOffsetHeight + compTabsHeight) + 'px'
//            });
//
//            components.css({
//                width: Math.min(300, Math.max(360, windowWidth / 4)) + 'px',
//                height: windowHeight - (headerOffsetHeight + compTabsHeight) + 'px'
//            });
//
//            elements.css({
//                width: Math.min(300, Math.max(360, windowWidth / 4)) + 'px',
//                height: windowHeight - (headerOffsetHeight + compTabsHeight) + 'px'
//            });

            if (previews) {
                
                previews.css({
                    width: windowWidth - rw + 'px',
                    height: win.height() - headerOffsetHeight + 'px'
                });

                $('.previewBox', previews).css({
                    width: windowWidth - rw + 'px',
                    height: windowHeight - (headerOffsetHeight + previewOffset) + 'px'
                });

                var iframes = $('.previewBox', previews).find('iframe');
                iframes.css({
                    width: $('.previewBox', previews).width() + 'px',
                    height: windowHeight - (headerOffsetHeight + previewOffset) + 'px'
                });
            }
        }

    },
    onload: function() {

        _Pages.init();

        activeTab = localStorage.getItem(activeTabKey);
        activeTabRight = localStorage.getItem(activeTabRightKey);
        log('value read from local storage', activeTab);

        log('onload');

        main.prepend('<div id="pages"></div><div id="previews"></div>'
                + '<div id="widgetsSlideout" class="slideOut"><div class="compTab" id="widgetsTab">Widgets</div></div>'
                + '<div id="palette" class="slideOut"><div class="compTab" id="paletteTab">HTML Palette</div></div>'
                + '<div id="components" class="slideOut"><div class="compTab" id="componentsTab">Reused Components</div></div>'
                + '<div id="elements" class="slideOut"><div class="compTab" id="elementsTab">Orphaned Elements</div></div>');

        pages = $('#pages');
        previews = $('#previews');
        widgetsSlideout = $('#widgetsSlideout');
        palette = $('#palette');
        components = $('#components');
        elements = $('#elements');

        //main.before('<div id="hoverStatus">Hover status</div>');

        $('#widgetsTab').on('click', function() {
            var l = widgetsSlideout.position().left;
            if (l+1 === $(window).width()) {
                $(this).addClass('active');
                _Pages.closeSlideOuts([palette, components, elements]);
                widgetsSlideout.animate({right : '+=425px'}, { duration: 200 }).zIndex(1);
                _Elements.reloadWidgets();
                localStorage.setItem(activeTabRightKey, $(this).prop('id'));
            } else {
                $(this).removeClass('active');
                widgetsSlideout.animate({right : '-=425px'}, { duration : 100 }).zIndex(2);
                localStorage.removeItem(activeTabRightKey);
            }
        });

        $('#paletteTab').on('click', function() {
            var l = palette.position().left;
            if (l+1 === $(window).width()) {
                $(this).addClass('active');
                _Pages.closeSlideOuts([widgetsSlideout, components, elements]);
                palette.animate({right : '+=425px'}, { duration: 200 }).zIndex(1);
                _Elements.reloadPalette();
                localStorage.setItem(activeTabRightKey, $(this).prop('id'));
            } else {
                $(this).removeClass('active');
                palette.animate({right : '-=425px'}, { duration : 100 }).zIndex(2);
                localStorage.removeItem(activeTabRightKey);
            }
        });

        $('#componentsTab').on('click', function() {
            var l = components.position().left;
            if (l+1 === $(window).width()) {
                $(this).addClass('active');
                _Pages.closeSlideOuts([widgetsSlideout, palette, elements]);
                _Elements.reloadComponents();
                components.animate({right : '+=425px'}, { duration: 200 }).zIndex(1);
                localStorage.setItem(activeTabRightKey, $(this).prop('id'));
            } else {
                $(this).removeClass('active');
                components.animate({right : '-=425px'}, { duration : 100 }).zIndex(2);
                localStorage.removeItem(activeTabRightKey);
            }
        }).droppable({
            accept: '.element, .content, .component, .file, .image, .widget',
            greedy: true,
            hoverClass: 'nodeHover',
            tolerance: 'pointer',
            over: function(e, ui) {
                e.stopPropagation();
                $('#componentsTab').droppable();
                $(this).addClass('active');
                $('#paletteTab').removeClass('active');
                $('#elementsTab').removeClass('active');
//                components.toggle('slide', {'direction' : 'right', 'distance' : '400px', 'speed' : 'fast' });
//                elements.toggle('slide', {'direction' : 'right', 'distance' : '400px', 'speed' : 'fast' });
//                palette.toggle('slide', {'direction' : 'right', 'distance' : '400px', 'speed' : 'fast' });
                _Elements.reloadComponents();
                localStorage.setItem(activeTabRightKey, $(this).prop('id'));
            }
        });

        $('#elementsTab').on('click', function() {
            var l = elements.position().left;
            if (l+1 === $(window).width()) {
                $(this).addClass('active');
                _Pages.closeSlideOuts([widgetsSlideout, palette, components], $(this));
                elements.animate({right : '+=425px'}, { duration: 200 }).zIndex(1);
                _Elements.reloadUnattachedNodes();
                localStorage.setItem(activeTabRightKey, $(this).prop('id'));
            } else {
                $(this).removeClass('active');
                elements.animate({right : '-=425px'}, { duration : 100 }).zIndex(2);
                localStorage.removeItem(activeTabRightKey);
            }

//            if (activeTabRight === 'elementsTab') {
//                $(this).addClass('active');
//                elements.animate({'right' : (l+1 === $(window).width() ? '+' : '-') + '=425px' });
//                _Elements.reloadUnattachedNodes();
//                //localStorage.setItem(activeTabRightKey, $(this).prop('id'));
//            }
        }).droppable({
            over: function(e, ui) {
                e.stopPropagation();
                $(this).addClass('active');
                $('#paletteTab').removeClass('active');
                $('#componentsTab').removeClass('active');
//                components.toggle('slide', {'direction' : 'right', 'distance' : '400px', 'speed' : 'fast' });
//                elements.toggle('slide', {'direction' : 'right', 'distance' : '400px', 'speed' : 'fast' });
//                palette.toggle('slide', {'direction' : 'right', 'distance' : '400px', 'speed' : 'fast' });
                _Elements.reloadUnattachedNodes();
                localStorage.setItem(activeTabRightKey, $(this).prop('id'));
            }
        });

        if (activeTabRight) {
            $('#' + activeTabRight).addClass('active').click();
        }

        $('#controls', main).remove();

        previews.append('<ul id="previewTabs"></ul>');
        previewTabs = $('#previewTabs', previews);

        _Pages.refresh();

        window.setTimeout('_Pages.resize()', 1000);

    },
    closeSlideOuts: function(slideout) {
      
        slideout.forEach(function(w) {
            
            var s = $(w);
            
            var l = s.position().left;
            if (l+1 !== $(window).width()) {
                //console.log('closing open slide-out', s);
                s.animate({right : '-=425px'}, { duration : 100 }).zIndex(2);
                $('.compTab.active', s).removeClass('active');
            }
        });
        
    },
    clearPreviews: function() {

        if (previewTabs && previewTabs.length) {
            previewTabs.children('.page').remove();
        }

    },
    refresh: function() {
        pages.empty();
        previewTabs.empty();

        Structr.addPager(pages, 'Page');

        previewTabs.append('<li id="import_page" class="button"><img class="add_button icon" src="icon/page_white_put.png"></li>');
        $('#import_page', previewTabs).on('click', function(e) {
            e.stopPropagation();

            Structr.dialog('Import Page', function() {
                return true;
            }, function() {
                return true;
            });

            dialog.empty();
            dialogMsg.empty();

            dialog.append('<h3>Create page from source code ...</h3>'
                    + '<textarea id="_code" name="code" cols="40" rows="10" placeholder="Paste HTML code here"></textarea>');

            dialog.append('<h3>... or fetch page from URL: <input id="_address" name="address" size="40" value="http://"></h3><table class="props">'
                    + '<tr><td><label for="name">Name of new page:</label></td><td><input id="_name" name="name" size="20"></td></tr>'
                    + '<tr><td><label for="timeout">Timeout (ms)</label></td><td><input id="_timeout" name="timeout" size="20" value="5000"></td></tr>'
                    + '<tr><td><label for="publicVisibilty">Visible to public</label></td><td><input type="checkbox" id="_publicVisible" name="publicVisibility"></td></tr>'
                    + '<tr><td><label for="authVisibilty">Visible to authenticated users</label></td><td><input type="checkbox" checked="checked" id="_authVisible" name="authVisibilty"></td></tr>'
                    + '</table>');

            var addressField = $('#_address', dialog);

            log('addressField', addressField);

            addressField.on('blur', function() {
                var addr = $(this).val().replace(/\/+$/, "");
                log(addr);
                $('#_name', dialog).val(addr.substring(addr.lastIndexOf("/") + 1));
            });


            dialog.append('<button id="startImport">Start Import</button>');

            $('#startImport').on('click', function(e) {
                e.stopPropagation();

                var code = $('#_code', dialog).val();
                var address = $('#_address', dialog).val();
                var name = $('#_name', dialog).val();
                var timeout = $('#_timeout', dialog).val();
                var publicVisible = $('#_publicVisible:checked', dialog).val() === 'on';
                var authVisible = $('#_authVisible:checked', dialog).val() === 'on';

                log('start');
                return Command.importPage(code, address, name, timeout, publicVisible, authVisible);
            });

        });

        previewTabs.append('<li id="add_page" class="button"><img class="add_button icon" src="icon/add.png"></li>');
        $('#add_page', previewTabs).on('click', function(e) {
            e.stopPropagation();
            //var entity = {};
            //entity.type = 'Page';
            //Command.create(entity);
            Command.createSimplePage();
        });

    },
    addTab: function(entity) {
        previewTabs.children().last().before('<li id="show_' + entity.id + '" class="page ' + entity.id + '_"></li>');

        var tab = $('#show_' + entity.id, previews);

        tab.append('<img class="typeIcon" src="icon/page.png"> <b title="' + entity.name + '" class="name_">' + fitStringToSize(entity.name, 200) + '</b>');
        tab.append('<img title="Delete page \'' + entity.name + '\'" alt="Delete page \'' + entity.name + '\'" class="delete_icon button" src="' + Structr.delete_icon + '">');
        tab.append('<img class="view_icon button" title="View ' + entity.name + ' in new window" alt="View ' + entity.name + ' in new window" src="icon/eye.png">');

        $('.view_icon', tab).on('click', function(e) {
            e.stopPropagation();
            var self = $(this);
            //var name = $(self.parent().children('b.name_')[0]).text();
            var link = $.trim(self.parent().children('b.name_').attr('title'));
            window.open(viewRootUrl + link);
        });

        var deleteIcon = $('.delete_icon', tab);
        deleteIcon.hide();
        deleteIcon.on('click', function(e) {
            e.stopPropagation();
            _Entities.deleteNode(this, entity);
        });
        deleteIcon.on('mouseover', function(e) {
            var self = $(this);
            self.show();

        });

        return tab;
    },
    resetTab: function(element) {

        log('resetTab', element);

        element.children('input').hide();
        element.children('.name_').show();

        var icons = $('.button', element);
        //icon.hide();

        element.hover(function(e) {
            icons.show();
        },
                function(e) {
                    icons.hide();
                });

        element.on('click', function(e) {
            e.stopPropagation();
            var self = $(this);
            var clicks = e.originalEvent.detail;
            if (clicks === 1) {
                log('click', self, self.css('z-index'));
                if (self.hasClass('active')) {
                    _Pages.makeTabEditable(self);
                } else {
                    _Pages.activateTab(self);
                }
            }
        });

        if (element.prop('id').substring(5) === activeTab) {
            _Pages.activateTab(element);
        }
    },
    activateTab: function(element) {

        //var name = $.trim(element.children('.name_').text());
        var name = $.trim(element.children('b.name_').attr('title'));
        log('activateTab', element, name);

        previewTabs.children('li').each(function() {
            $(this).removeClass('active');
        });

        $('.previewBox', previews).each(function() {
            $(this).hide();
        });

        var id = element.prop('id').substring(5);
        activeTab = id;

        _Pages.reloadIframe(id, name);

        element.addClass('active');

        log('store active tab', activeTab);
        localStorage.setItem(activeTabKey, activeTab);

    },
    reloadIframe: function(id, name) {
        var iframe = $('#preview_' + id);
        log(iframe);
        iframe.prop('src', viewRootUrl + name + '?edit=2');
        iframe.parent().show();
        iframe.on('load', function() {
            log('iframe loaded', $(this));
        });
        _Pages.resize();

    },
    makeTabEditable: function(element) {
        //element.off('dblclick');

        var id = element.prop('id').substring(5);

        element.off('hover');
        //var oldName = $.trim(element.children('.name_').text());
        var oldName = $.trim(element.children('b.name_').attr('title'));
        element.children('b').hide();
        element.find('.button').hide();
        var input = $('input.newName_', element);

        if (!input.length) {
            element.append('<input type="text" size="' + (oldName.length + 4) + '" class="newName_" value="' + oldName + '">');
            input = $('input', element);
        }

        input.show().focus().select();

        input.on('blur', function() {
            log('blur');
            var self = $(this);
            var newName = self.val();
            Command.setProperty(id, "name", newName);
            _Pages.resetTab(element, newName);
        });

        input.keypress(function(e) {
            if (e.keyCode === 13 || e.keyCode === 9) {
                e.preventDefault();
                log('keypress');
                var self = $(this);
                var newName = self.val();
                Command.setProperty(id, "name", newName);
                _Pages.resetTab(element, newName);
            }
        });

        element.off('click');

    },
    appendPageElement: function(entity) {

        var hasChildren = entity.childElements.length;

        pages.append('<div id="id_' + entity.id + '" class="node page"></div>');
        var div = Structr.node(entity.id);

        $('.button', div).on('mousedown', function(e) {
            e.stopPropagation();
        });

        div.append('<img class="typeIcon" src="icon/page.png">'
                + '<b title="' + entity.name + '" class="name_">' + fitStringToSize(entity.name, 200) + '</b> <span class="id">' + entity.id + '</span>');

        _Entities.appendExpandIcon(div, entity, hasChildren);
        _Entities.appendAccessControlIcon(div, entity);

        div.append('<img title="Delete page \'' + entity.name + '\'" alt="Delete page \'' + entity.name + '\'" class="delete_icon button" src="' + Structr.delete_icon + '">');
        $('.delete_icon', div).on('click', function(e) {
            e.stopPropagation();
            _Entities.deleteNode(this, entity);
        });

        _Entities.appendEditPropertiesIcon(div, entity);
        _Entities.setMouseOver(div);

        var tab = _Pages.addTab(entity);

        previews.append('<div class="previewBox"><iframe id="preview_'
                + entity.id + '"></iframe></div><div style="clear: both"></div>');

        _Pages.resetTab(tab, entity.name);

        $('#preview_' + entity.id).hover(function() {
            var self = $(this);
            var elementContainer = self.contents().find('.structr-element-container');
            elementContainer.addClass('structr-element-container-active');
            elementContainer.removeClass('structr-element-container');
        }, function() {
            var self = $(this);
            var elementContainer = self.contents().find('.structr-element-container-active');
            elementContainer.addClass('structr-element-container');
            elementContainer.removeClass('structr-element-container-active');
            //self.find('.structr-element-container-header').remove();
        });

        $('#preview_' + entity.id).load(function() {

            //var offset = $(this).offset();

            var doc = $(this).contents();
            var head = $(doc).find('head');
            if (head)
                head.append('<style media="screen" type="text/css">'
                        + '* { z-index: 0}\n'
                        + '.nodeHover { border: 1px dotted red; }\n'
                        //+ '.structr-content-container { display: inline-block; border: none; margin: 0; padding: 0; min-height: 10px; min-width: 10px; }\n'
                        + '.structr-content-container { min-height: .25em; min-width: .25em; }\n'
                        //		+ '.structr-element-container-active { display; inline-block; border: 1px dotted #e5e5e5; margin: -1px; padding: -1px; min-height: 10px; min-width: 10px; }\n'
                        //		+ '.structr-element-container { }\n'
                        + '.structr-element-container-active:hover { border: 1px dotted red ! important; }\n'
                        + '.structr-droppable-area { border: 1px dotted red ! important; }\n'
                        + '.structr-editable-area { border: 1px dotted orange ! important; }\n'
                        + '.structr-editable-area-active { background-color: #ffe; border: 1px solid orange ! important; color: #333; margin: -1px; padding: 1px; }\n'
                        //		+ '.structr-element-container-header { font-family: Arial, Helvetica, sans-serif ! important; position: absolute; font-size: 8pt; }\n'
                        + '.structr-element-container-header { font-family: Arial, Helvetica, sans-serif ! important; position: absolute; font-size: 8pt; color: #333; border-radius: 5px; border: 1px solid #a5a5a5; padding: 3px 6px; margin: 6px 0 0 0; background-color: #eee; background: -webkit-gradient(linear, left bottom, left top, from(#ddd), to(#eee)) no-repeat; background: -moz-linear-gradient(90deg, #ddd, #eee) no-repeat; filter: progid:DXImageTransform.Microsoft.Gradient(StartColorStr="#eeeeee", EndColorStr="#dddddd", GradientType=0);\n'
                        + '.structr-element-container-header img { width: 16px ! important; height: 16px ! important; }\n'
                        + '.link-hover { border: 1px solid #00c; }\n'
                        + '.edit_icon, .add_icon, .delete_icon, .close_icon, .key_icon {  cursor: pointer; heigth: 16px; width: 16px; vertical-align: top; float: right;  position: relative;}\n'
                        + '</style>');

            var iframeDocument = $(this).contents();
            //var iframeWindow = this.contentWindow;

            var droppables = iframeDocument.find('[data-structr_element_id]');

            if (droppables.length === 0) {

                //iframeDocument.append('<html structr_element_id="' + entity.id + '">dummy element</html>');
                var html = iframeDocument.find('html');
                html.attr('data-structr_element_id', entity.id);
                html.addClass('structr-element-container');

            }
//            droppables = iframeDocument.find('[data-structr_element_id]');
//
//            droppables.each(function(i,element) {
//                var el = $(element);
//
//                el.droppable({
//                    accept: '.element, .content, .component',
//                    greedy: true,
//                    hoverClass: 'structr-droppable-area',
//// this requires a patched jQuery, which we won't do anymore
//// TODO: Find a better solution for dropping elements in the iframe at the right place                    
////                    iframeOffset: { 
////                        'top' : offset.top,
////                        'left' : offset.left
////                    },
//                    drop: function(event, ui) {
//                        
//                        var self = $(this);
//                        var page = self.closest( '.page')[0];
//                        var pageId;
//                        var pos;
//                        
//                        if (page) {
//
//                            // we're in the main page
//                            pageId = getId(page);
//                            pos = $('.content, .element', self).length;
//
//                        } else {
//                            
//                            // we're in the iframe
//                            page = self.closest('[data-structr_page_id]')[0];
//                            pageId = $(page).attr('data-structr_page_id');
//                            pos = $('[data-structr_element_id]', self).length;
//                        }
//                        
////                        var contentId = getId(ui.draggable);
//                        var elementId = getId(self);
//
//                        if (!elementId) elementId = self.attr('data-structr_element_id');
//
////                        if (!contentId) {
////                            // create element on the fly
////                            var tag = $(ui.draggable).text();
////                            
////                            console.log('suppress dropping anything in preview iframes for now');
////                        //Command.createAndAppendDOMNode(pageId, elementId, (tag != 'content' ? tag : ''));
////                            
////                        } else {
////                            console.log('suppress dropping anything in preview iframes for now');
////                        //Command.appendChild(contentId, elementId);
////                        }
//                    }
//                });
//
//                var structrId = el.attr('data-structr_element_id');
//                //var type = el.prop('structr_type');
//                //  var name = el.prop('structr_name');
////                var tag  = element.nodeName.toLowerCase();
//                if (structrId) {
//
//                    $('.move_icon', el).on('mousedown', function(e) {
//                        e.stopPropagation();
//                        var self = $(this);
//                        var element = self.closest('[data-structr_element_id]');
//                        //var element = self.children('.structr-node');
//                        log(element);
//                        var entity = Structr.entity(structrId, element.prop('data-structr_element_id'));
//                        entity.type = element.prop('data-structr_type');
//                        entity.name = element.prop('data-structr_name');
//                        log('move', entity);
//                        //var parentId = element.prop('structr_element_id');
//                        self.parent().children('.structr-node').show();
//                    });
//
//                    //                    $('b', el).on('click', function(e) {
//                    //                        e.stopPropagation();
//                    //                        var self = $(this);
//                    //                        var element = self.closest('[data-structr_element_id]');
//                    //                        var entity = Structr.entity(structrId, element.prop('structr_element_id'));
//                    //                        entity.type = element.prop('structr_type');
//                    //                        entity.name = element.prop('structr_name');
//                    //                        log('edit', entity);
//                    //                        //var parentId = element.prop('structr_element_id');
//                    //                        log(element);
//                    ////                        Structr.dialog('Edit Properties of ' + entity.id, function() {
//                    ////                            log('save')
//                    ////                        }, function() {
//                    ////                            log('cancelled')
//                    ////                        });
//                    //                        _Entities.showProperties(entity);
//                    //                    });
//
//                    $('.delete_icon', el).on('click', function(e) {
//                        e.stopPropagation();
//                        var self = $(this);
//                        var element = self.closest('[data-structr_element_id]');
//                        var entity = Structr.entity(structrId, element.prop('structr_element_id'));
//                        entity.type = element.prop('data-structr_type');
//                        entity.name = element.prop('data-structr_name');
//                        log('delete', entity);
//                        var parentId = element.prop('data-structr_element_id');
//
//                        Command.removeSourceFromTarget(entity.id, parentId);
//                        _Entities.deleteNode(this, entity);
//                    });
//                    var offsetTop = -30;
//                    var offsetLeft = 0;
//                    el.on({
//                        mouseover: function(e) {
//                            e.stopPropagation();
//                            var self = $(this);
//                            //self.off('click');
//
//                            self.addClass('structr-element-container-active');
//
//                            //                            self.parent().children('.structr-element-container-header').remove();
//                            //
//                            //                            self.append('<div class="structr-element-container-header">'
//                            //                                + '<img class="typeIcon" src="/structr/'+ _Elements.icon + '">'
//                            //                                + '<b class="name_">' + name + '</b> <span class="id">' + structrId + '</b>'
//                            //                                + '<img class="delete_icon structr-container-button" title="Delete ' + structrId + '" alt="Delete ' + structrId + '" src="/structr/icon/delete.png">'
//                            //                                + '<img class="edit_icon structr-container-button" title="Edit properties of ' + structrId + '" alt="Edit properties of ' + structrId + '" src="/structr/icon/application_view_detail.png">'
//                            //                                + '<img class="move_icon structr-container-button" title="Move ' + structrId + '" alt="Move ' + structrId + '" src="/structr/icon/arrow_move.png">'
//                            //                                + '</div>'
//                            //                                );
//
//                            var node = Structr.node(structrId);
//                            if (node) {
//                                node.parent().removeClass('nodeHover');
//                                node.addClass('nodeHover');
//                            }
//
//                            var pos = self.position();
//                            var header = self.children('.structr-element-container-header');
//                            header.css({
//                                position: "absolute",
//                                top: pos.top + offsetTop + 'px',
//                                left: pos.left + offsetLeft + 'px',
//                                cursor: 'pointer'
//                            }).show();
//                            log(header);
//                        },
//                        mouseout: function(e) {
//                            e.stopPropagation();
//                            var self = $(this);
//                            self.removeClass('.structr-element-container');
//                            var header = self.children('.structr-element-container-header');
//                            header.remove();
//                            var node = Structr.node(structrId);
//                            if (node) {
//                                node.removeClass('nodeHover');
//                            }
//                        }
//                    });
//
//                }
//            });

            //$(this).contents().find('[data-structr-id]').each(function(i,element) {
            $(this).contents().find('*').each(function(i, element) {

                getComments(element).forEach(function(c) {

                    var inner = $(getNonCommentSiblings(c.textNode));
                    $(getNonCommentSiblings(c.textNode)).remove();
                    $(c.textNode).replaceWith('<div data-structr-id="' + c.id + '" data-structr-raw-content="' + c.rawContent + '">' + c.textNode.nodeValue + '</div>');

                    var el = $(element).children('[data-structr-id="' + c.id + '"]');
                    
                    el.append(inner);

                    $(el).on({
                        mouseover: function(e) {
                            e.stopPropagation();
                            var self = $(this);
                            //self.replaceWith('<span data-structr-id="' + id + '">' + c.nodeValue + '</span>');

                            self.addClass('structr-editable-area');
                            //$('#hoverStatus').text('Editable content element: ' + self.attr('data-structr_content_id'));
                            var contentSourceId = self.attr('data-structr-id');
                            var node = Structr.node(contentSourceId);
                            if (node) {
                                node.parent().removeClass('nodeHover');
                                node.addClass('nodeHover');
                            }
                        },
                        mouseout: function(e) {
                            e.stopPropagation();
                            var self = $(this);
                            //swapFgBg(self);
                            self.removeClass('structr-editable-area');
                            //self.prop('contenteditable', false);
                            //$('#hoverStatus').text('-- non-editable --');
                            var contentSourceId = self.attr('data-structr-id');
                            var node = Structr.node(contentSourceId);
                            if (node) {
                                node.removeClass('nodeHover');
                            }
                        },
                        click: function(e) {
                            e.stopPropagation();
                            e.preventDefault();
                            var self = $(this);
                            if (self.hasClass('structr-editable-area-active')) {
                                return false;
                            }
                            self.removeClass('structr-editable-area').addClass('structr-editable-area-active').prop('contenteditable', true);

                            // Store old text in global var
                            textBeforeEditing = self.text();//cleanText(self.contents());

                            //var srcText = expandNewline(self.attr('data-structr-raw-content'));
                            var srcText = expandNewline(self.attr('data-structr-raw-content'));
                            // Replace only if it differs (e.g. for variables)
                            if (srcText !== textBeforeEditing) {
                                self.html(srcText);
                                textBeforeEditing = srcText;
                            }
                            return false;

                        },
                        blur: function(e) {
                            e.stopPropagation();
                            var self = $(this);
                            var contentSourceId = self.attr('data-structr-id');
                            var text = cleanText(self.html());
                            //Command.patch(contentSourceId, textBeforeEditing, text);
                            Command.setProperty(contentSourceId, 'content', text);
                            contentSourceId = null;
                            self.attr('contenteditable', false);
                            self.removeClass('structr-editable-area-active').removeClass('structr-editable-area');
                            _Pages.reloadPreviews();
                        }
                    });

                });

            });

        });

        div.droppable({
            accept: '#add_html, .html_element',
            greedy: true,
            hoverClass: 'nodeHover',
            tolerance: 'pointer',
            drop: function(event, ui) {
                var self = $(this);
                console.log('dropped onto', self);
                // Only html elements are allowed, and only if none exists

                if (getId(self) === getId(sortParent))
                    return false;

                _Entities.ensureExpanded(self);
                sorting = false;
                sortParent = undefined;

                var nodeData = {};

                var page = self.closest('.page')[0];

                var contentId = getId(ui.draggable);
                var elementId = getId(self);
                console.log('elementId', elementId);

                var source = StructrModel.obj(contentId);
                var target = StructrModel.obj(elementId);

                if (source && getId(page) && source.pageId && getId(page) !== source.pageId) {
                    event.preventDefault();
                    event.stopPropagation();
                    Command.copyDOMNode(source.id, target.id);
                    //_Entities.showSyncDialog(source, target);
                    _Elements.reloadComponents();
                    return;
                } else {
                    log('not copying node');
                }

                if (contentId === elementId) {
                    log('drop on self not allowed');
                    return;
                }

                var tag;
                var cls = Structr.getClass($(ui.draggable));

                if (!contentId) {
                    tag = $(ui.draggable).text();

                    if (tag !== 'html') {
                        return false;
                    }

                    var pageId = (page ? getId(page) : target.pageId);

                    Command.createAndAppendDOMNode(pageId, elementId, (tag !== 'content' ? tag : ''), nodeData);
                    return;

                } else {
                    tag = cls;
                    log('appendChild', contentId, elementId);
                    sorting = false;
                    Command.appendChild(contentId, elementId);
                    //$(ui.draggable).remove();

                    return;
                }
                log('drop event in appendPageElement', getId(page), getId(self), (tag !== 'content' ? tag : ''));
            }
        });

        return div;

    },
    appendElementElement: function(entity, refNode, refNodeIsParent) {
        log('_Pages.appendElementElement(', entity, refNode, refNodeIsParent, ');')
        var div = _Elements.appendElementElement(entity, refNode, refNodeIsParent);
        if (!div)
            return false;

        var parentId = entity.parent && entity.parent.id;
        if (parentId) {
            $('.delete_icon', div).replaceWith('<img title="Remove" '
                    + 'alt="Remove" class="delete_icon button" src="icon/brick_delete.png">');
            $('.button', div).on('mousedown', function(e) {
                e.stopPropagation();
            });
            $('.delete_icon', div).on('click', function(e) {
                e.stopPropagation();
                Command.removeChild(entity.id);
            });
        }

        var sortableOptions = {
            sortable: '.node',
            appendTo: '#main',
            helper: 'clone',
            zIndex: 99,
            containment: 'body',
            start: function(event, ui) {
                sorting = true;
                sortParent = $(ui.item).parent();
            },
            update: function(event, ui) {

                var el = $(ui.item);
                //console.log('### sortable update: sorting?', sorting, getId(el), getId(self), getId(sortParent));
                if (!sorting)
                    return false;

                var id = getId(el);
                if (!id)
                    id = getComponentId(el);

                var nextNode = el.next('.node');
                var refId = getId(nextNode);
                if (!refId)
                    refId = getComponentId(nextNode);

                var parentId = getId(sortParent);
                el.remove();
                Command.insertBefore(parentId, id, refId);
                sorting = false;
                sortParent = undefined;
                _Pages.reloadPreviews();
            },
            stop: function(event, ui) {
                //$(ui.sortable).remove();
                sorting = false;
                _Entities.resetMouseOverState(ui.item);
            }
        };

        div.sortable(sortableOptions);

        div.droppable({
            accept: '.node, .element, .content, .image, .file, .widget',
            greedy: true,
            hoverClass: 'nodeHover',
            tolerance: 'pointer',
            drop: function(event, ui) {

                div.sortable('refresh');

                var self = $(this);
                log('dropped onto', self, getId(self), getId(sortParent));
                if (getId(self) === getId(sortParent))
                    return false;

                _Entities.ensureExpanded(self);
                sorting = false;
                sortParent = undefined;

                var nodeData = {};

                var sourceId = getId(ui.draggable) || getComponentId(ui.draggable);
                var elementId = getId(self);

                var source = StructrModel.obj(sourceId);
                var target = StructrModel.obj(elementId);

                var page = self.closest('.page')[0];
                var pageId = (page ? getId(page) : target.pageId);

                log(sourceId, source, pageId);

                if (source && pageId && source.pageId && pageId !== source.pageId) {
                    event.preventDefault();
                    event.stopPropagation();
                    Command.copyDOMNode(source.id, target.id);
                    //_Entities.showSyncDialog(source, target);
                    _Elements.reloadComponents();
                    return;
                } else {
                    log('not copying node');
                }

                if (sourceId === elementId) {
                    log('drop on self not allowed');
                    return;
                }

                var tag, name;

                if (source && source.type === 'Widget') {

                    var baseUrl = 'http://' + remoteHost + ':' + remotePort;

                    Structr.modules['widgets'].unload();
                    _Pages.makeMenuDroppable();

                    //var pattern = /^\[[a-zA-Z]+\]$/;
                    var pattern = /\[[a-zA-Z]+\]/g;
                    var dCssPattern = /\[!d-cssStyle\]/g;
                    
                    var text = source.source;
                    if (text) {
                        
                        var rawMatches = text.match(pattern);
                        var dRawCssatches = text.match(dCssPattern);

                        if (rawMatches || dRawCssatches) {

                            var matches,
                                dCssMatches;
                        
                            if(rawMatches)
                                matches = $.unique(rawMatches);
                            if(dRawCssatches)
                                dCssMatches = $.unique(dRawCssatches);
                            
                            if ((matches && matches.length) || (dCssMatches && dCssMatches.length)) {

                                Structr.dialog('Configure Widget', function() {
                                }, function() {
                                });

                                dialogText.append('<p>Fill out the following parameters to correctly configure the widget.</p><table class="props"></table>');
                                var table = $('table', dialogText);
                                
                                //Input fields [inputName]
                                if(matches){
                                    $.each(matches, function(i, match) {

                                        var label = _Crud.formatKey(match.replace(/\[/, '').replace(/\]/, ''));
                                        table.append('<tr><td><label for="' + label + '">' + label + '</label></td><td><input type="text" id="' + match + '" placeholder="' + label + '"></td></tr>');

                                    });
                                }
                                
                                /* 
                                 * design field
                                 * change the style width
                                 * [!d-cssStyle]
                                 */
                                if(dCssMatches){
                                    $.each(dCssMatches, function(i, match){
                                                       
                                        var dButton = $('<button style="margin-bottom:1em;"> Open </button>');
                                        var dArea = $('<div style="display:none;"></div>');
                                        var dElement = $('<div id="designIn'+i+'" style="background-color:grey;color:black;height:2em;width:10em">Preview</div>');
                                        var dTextArea = $('<textarea cols="20" rows="5" style="margin-top:1em;">background-color:grey;\ncolor:black;\nwidth:10em;\nheight:2em;</textarea>');
                                        var dTestCssButton = $('<button>Apply Changes</button>');
                                        
                                        dTestCssButton.click(function(){
                                            dButton.html(" Close ");
                                            var textAreaValue = dTextArea.val();
                                            var tAreaSplit = textAreaValue.split("\n");
                                            
                                            $.each(tAreaSplit, function(){
                                                var cssAttr = this.toString().split(":")[0];
                                                var cssVal = this.toString().split(":")[1];
                                                
                                                if(cssVal.charAt(cssVal.length-1) === ';')
                                                    cssVal = cssVal.substr(0,cssVal.length-1);
                                                
                                                dElement.css(cssAttr,cssVal);
                                                
                                            });
                                            cssStyle = dElement.attr("style");
                                        });
                                        
                                        dArea.append(dElement);
                                        dArea.append(dTextArea);
                                        dArea.append(dTestCssButton);
                                        
                                        var open=true;
                                        dButton.click(function(){
                                            dArea.toggle(200);
                                            open=!open;
                                            if(open)
                                                dButton.html(" Open ");
                                            else
                                                dButton.html(" Close ");
                                        });
                                        
                                        table.append('<tr><td>Design</td><td id="design'+i+'"></td></tr>');
                                        $("#design"+i).append(dButton);
                                        $('#design'+i).append(dArea);
                                    });
                                }

                                dialog.append('<button id="appendWidget">Append Widget</button>');
                                var attrs = {};
                                $('#appendWidget').on('click', function(e) {

                                    if(matches)
                                        $.each(matches, function(i, match) {

                                            $.each($('input[type="text"]', table), function(i, m) {
                                                var key = $(m).prop('id').replace(/\[/, '').replace(/\]/, '')
                                                attrs[key] = $(this).val();
                                                //console.log(this, match, key, attrs[key]);
                                            });

                                        });
                                    
                                    if(dCssMatches)
                                        $.each(dCssMatches,function(i,match){
                                            text=text.replace(dCssPattern,$('#designIn'+i).attr("style"));
                                        });

                                    //console.log(source.source, elementId, pageId, attrs);
                                    e.stopPropagation();
                                    Command.appendWidget(text, elementId, pageId, baseUrl, attrs);

                                    dialogCancelButton.click();
                                    $(ui.draggable).remove();
                                    return;
                                });

                            }

                        }else{

                            // If no matches, directly append widget
                            Command.appendWidget(source.source, elementId, pageId, baseUrl);

                        }

                    }

                    $(ui.draggable).remove();
                    return;

                } else if (source && source.type === 'Image') {

                    sourceId = undefined;
                    name = $(ui.draggable).find('.name_').attr('title');
                    log('Image dropped, creating <img> node', name);
                    nodeData._html_src = '/' + name;
                    nodeData.name = name;
                    tag = 'img';

                    Structr.modules['images'].unload();
                    _Pages.makeMenuDroppable();

                    Command.createAndAppendDOMNode(getId(page), elementId, tag, nodeData);
                    $(ui.draggable).remove();

                    return;

                } else if (source && source.type === 'File') {

                    name = $(ui.draggable).children('.name_').attr('title');

                    var parentTag = self.children('.tag_').text();
                    log(parentTag);
                    nodeData.linkableId = sourceId;

                    if (parentTag === 'head') {

                        log('File dropped in <head>');

                        if (name.endsWith('.css')) {

                            //console.log('CSS file dropped in <head>, creating <link>');

                            tag = 'link';
                            nodeData._html_href = '/${link.name}';
                            nodeData._html_type = 'text/css';
                            nodeData._html_rel = 'stylesheet';
                            nodeData._html_media = 'screen';


                        } else if (name.endsWith('.js')) {

                            log('JS file dropped in <head>, creating <script>');

                            tag = 'script';
                            nodeData._html_src = '/${link.name}';
                            nodeData._html_type = 'text/javascript';
                        }

                    } else {

                        log('File dropped, creating <a> node', name);
                        nodeData._html_href = '/${link.name}';
                        nodeData._html_title = '${link.name}';
                        nodeData.childContent = '${parent.link.name}';
                        tag = 'a';
                    }
                    sourceId = undefined;

                    Structr.modules['files'].unload();
                    _Pages.makeMenuDroppable();

                    Command.createAndAppendDOMNode(pageId, elementId, tag, nodeData);

                    $(ui.draggable).remove();
                    return;
                }

                if (!sourceId) {

                    tag = $(ui.draggable).text();

                    if (tag === 'a' || tag === 'p'
                            || tag === 'h1' || tag === 'h2' || tag === 'h3' || tag === 'h4' || tag === 'h5' || tag === 'h5'
                            || tag === 'li' || tag === 'em' || tag === 'title' || tag === 'b' || tag === 'span' || tag === 'th' || tag === 'td' || tag === 'button') {

                        if (tag === 'a') {
                            nodeData._html_href = '/${link.name}';
                            nodeData.childContent = '${parent.link.name}';
                        } else if (tag === 'title') {
                            nodeData.childContent = '${page.name}';
                        } else {
                            nodeData.childContent = 'Initial text for ' + tag;
                        }

                        // set as expanded in advance
                        //addExpandedNode(contentId);

                    }

                    Command.createAndAppendDOMNode(pageId, elementId, (tag !== 'content' ? tag : ''), nodeData);
                    return;

                } else {
                    tag = Structr.getClass($(ui.draggable));
                    log('appendChild', sourceId, elementId);
                    sorting = false;
                    Command.appendChild(sourceId, elementId);
                    $(ui.draggable).remove();

                    return;
                }

                log('drop event in appendElementElement', pageId, getId(self), (tag !== 'content' ? tag : ''));
            }
        });
        return div;
    },
    reloadPreviews: function() {

        // add a small delay to avoid getting old data in very fast localhost envs
        window.setTimeout(function() {

            $('iframe', $('#previews')).each(function() {
                var self = $(this);
                var pageId = self.prop('id').substring('preview_'.length);

                if (pageId === activeTab) {
                    var doc = this.contentDocument;
                    doc.location.reload(true);
                }

            });
        }, 100);
    },
    zoomPreviews: function(value) {
        $('.previewBox', previews).each(function() {
            var val = value / 100;
            var box = $(this);

            box.css('-moz-transform', 'scale(' + val + ')');
            box.css('-o-transform', 'scale(' + val + ')');
            box.css('-webkit-transform', 'scale(' + val + ')');

            var w = origWidth * val;
            var h = origHeight * val;

            box.width(w);
            box.height(h);

            $('iframe', box).width(w);
            $('iframe', box).height(h);

            log("box,w,h", box, w, h);

        });

    },
    makeMenuDroppable: function() {

        $('#pages_').droppable({
            accept: '.element, .content, .component, .file, .image, .widget',
            greedy: true,
            hoverClass: 'nodeHover',
            tolerance: 'pointer',
            over: function(e, ui) {

                e.stopPropagation();
                $('#pages_').droppable('disable');
                log('over is off');

                Structr.activateMenuEntry('pages');
                window.location.href = '/structr/#pages';

                if (files && files.length)
                    files.hide();
                if (folders && folders.length)
                    folders.hide();
                if (widgets && widgets.length)
                    widgets.hide();

//                _Pages.init();
                Structr.modules['pages'].onload();
                _Pages.resize();
            }

        });

        $('#pages_').removeClass('nodeHover').droppable('enable');
    }
};

function getComments(el) {
    var comments = [];
    var f = el.firstChild;
    while (f) {
        if (f.nodeType === 8) {
            var id = f.nodeValue.extractVal('data-structr-id');
            var raw = f.nodeValue.extractVal('data-structr-raw-value');
            if (id) {
                f = f.nextSibling;
                if (f && f.nodeType === 3) {
                    var comment = {};
                    comment.id = id;
                    comment.textNode = f;
                    comment.rawContent = raw;
                    comments.push(comment);
                }
            }
        }
        f = f.nextSibling;
    }
    return comments;
}

function getNonCommentSiblings(el) {
    var siblings = [];
    var s = el.nextSibling;
    while (s) {
        if (s.nodeType === 8) {
            return siblings;
        }
        siblings.push(s);
        s = s.nextSibling;
    }
}